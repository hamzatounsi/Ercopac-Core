package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.ticketing.domain.*;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.repository.*;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

// Ticket categories and custom statuses of the current organisation (Ticketing settings).
// System categories are the TicketCategory values: always available, renamable, never deletable.
// Custom statuses map onto a system lifecycle status (TicketStatus); the system statuses cannot be deleted.
// Everyone in the Ticketing app reads them; only support leads change them.
@Service @Transactional
public class TicketConfigService {
 private static final Set<String> COLORS = Set.of("blue", "amber", "red", "green", "gray", "purple", "coral");
 private static final Map<TicketCategory, String> SYSTEM_LABELS = new EnumMap<>(Map.of(
   TicketCategory.SOFTWARE, "Software", TicketCategory.ACCESS, "Access", TicketCategory.PERFORMANCE, "Performance", TicketCategory.BILLING, "Billing",
   TicketCategory.DATA, "Data", TicketCategory.INTEGRATION, "Integration", TicketCategory.FEATURE_REQUEST, "Feature request", TicketCategory.OTHER, "Other"));

 private final TicketCategorySettingRepository categories; private final TicketCustomStatusRepository customStatuses; private final TicketRepository tickets; private final TicketSettingsService settings;
 public TicketConfigService(TicketCategorySettingRepository categories, TicketCustomStatusRepository customStatuses, TicketRepository tickets, TicketSettingsService settings) {
  this.categories = categories; this.customStatuses = customStatuses; this.tickets = tickets; this.settings = settings;
 }

 // ---------- categories ----------
 /** System categories (stored lazily, so their label can be changed) followed by the custom ones. */
 public List<CategoryDto> categories() { Long org = orgOf(settings.current()); return categoriesOf(org).stream().map(c -> category(c, org)).toList(); }

 public CategoryDto createCategory(CategoryRequest r) {
  AppUser lead = settings.requireLead(); Long org = lead.getOrganisation().getId();
  String code = code(r.code() == null || r.code().isBlank() ? r.label() : r.code(), 32);
  if (isSystemCode(code) || categories.findByOrganisation_IdAndCodeIgnoreCase(org, code).isPresent()) throw conflict("Category code " + code + " is already used.");
  TicketCategorySetting c = new TicketCategorySetting(); c.setOrganisation(lead.getOrganisation()); c.setCode(code); c.setSystem(false);
  c.setLabel(r.label().trim()); c.setActive(r.active() == null || r.active()); c.setSortOrder(100 + categoriesOf(org).size());
  return category(categories.save(c), org);
 }

 /** Renames a category (system ones included) or (de)activates a custom one; the code never changes. */
 public CategoryDto updateCategory(Long id, CategoryRequest r) {
  Long org = orgOf(settings.requireLead()); categoriesOf(org);
  TicketCategorySetting c = categories.findByIdAndOrganisation_Id(id, org).orElseThrow(() -> notFound("Category not found."));
  c.setLabel(r.label().trim());
  if (r.active() != null) { if (c.isSystem() && !r.active()) throw bad("System categories cannot be deactivated."); c.setActive(r.active()); }
  return category(categories.save(c), org);
 }

 /** Deletes a custom category. When tickets use it, reassignTo (another category code) is required and the tickets are moved first. */
 public void deleteCategory(Long id, String reassignTo) {
  Long org = orgOf(settings.requireLead());
  TicketCategorySetting c = categories.findByIdAndOrganisation_Id(id, org).orElseThrow(() -> notFound("Category not found."));
  if (c.isSystem()) throw conflict("System categories cannot be deleted.");
  long used = tickets.countByOrganisation_IdAndCategory(org, c.getCode());
  if (used > 0) {
   if (reassignTo == null || reassignTo.isBlank()) throw conflict("This category is used by " + used + " ticket(s); choose a category to move them to.");
   String target = requireCategory(org, reassignTo);
   if (target.equalsIgnoreCase(c.getCode())) throw bad("Choose another category to move the tickets to.");
   tickets.reassignCategory(org, c.getCode(), target);
  }
  categories.delete(c);
 }

 /** Validates a category code for a ticket of the organisation and returns its canonical code. */
 public String requireCategory(Long org, String code) {
  if (code == null || code.isBlank()) throw bad("A category is required.");
  String c = code.trim();
  for (TicketCategory s : TicketCategory.values()) if (s.name().equalsIgnoreCase(c)) return s.name();
  return categories.findByOrganisation_IdAndCodeIgnoreCase(org, c).filter(TicketCategorySetting::isActive).map(TicketCategorySetting::getCode)
    .orElseThrow(() -> bad("Unknown ticket category " + c + "."));
 }

 // ---------- custom statuses ----------
 public List<CustomStatusDto> customStatuses() { Long org = orgOf(settings.current()); return customStatuses.findByOrganisation_IdOrderByLabelAsc(org).stream().map(this::status).toList(); }

 public CustomStatusDto createStatus(CustomStatusRequest r) {
  AppUser lead = settings.requireLead(); Long org = lead.getOrganisation().getId();
  String code = code(r.code() == null || r.code().isBlank() ? r.label() : r.code(), 40);
  if (isSystemStatus(code) || customStatuses.existsByOrganisation_IdAndCodeIgnoreCase(org, code)) throw conflict("Status code " + code + " is already used.");
  TicketCustomStatus s = new TicketCustomStatus(); s.setOrganisation(lead.getOrganisation()); s.setCode(code); apply(s, r);
  return status(customStatuses.save(s));
 }

 public CustomStatusDto updateStatus(Long id, CustomStatusRequest r) {
  Long org = orgOf(settings.requireLead());
  TicketCustomStatus s = customStatuses.findByIdAndOrganisation_Id(id, org).orElseThrow(() -> notFound("Status not found."));
  // The lifecycle status of a status in use cannot change: tickets would silently change lifecycle state
  if (r.baseStatus() != s.getBaseStatus() && tickets.countByCustomStatus_Id(id) > 0) throw conflict("This status is used by tickets; its lifecycle status cannot change.");
  apply(s, r);
  return status(customStatuses.save(s));
 }

 /** Deletes a custom status; tickets using it fall back to its lifecycle (system) status, which they already have. */
 public void deleteStatus(Long id) {
  Long org = orgOf(settings.requireLead());
  TicketCustomStatus s = customStatuses.findByIdAndOrganisation_Id(id, org).orElseThrow(() -> notFound("Status not found."));
  tickets.clearCustomStatus(s.getId());
  customStatuses.delete(s);
 }

 public TicketCustomStatus requireStatus(Long id, Long org) { return customStatuses.findByIdAndOrganisation_Id(id, org).orElseThrow(() -> bad("Unknown ticket status.")); }

 // ---------- helpers ----------
 /** Stored categories of the organisation; the system ones are created on first use so they can be renamed. */
 private List<TicketCategorySetting> categoriesOf(Long org) {
  List<TicketCategorySetting> saved = new ArrayList<>(categories.findByOrganisation_IdOrderBySortOrderAscIdAsc(org));
  int order = 0;
  for (TicketCategory s : TicketCategory.values()) {
   final int sort = order++;
   if (saved.stream().noneMatch(c -> c.getCode().equalsIgnoreCase(s.name()))) {
    TicketCategorySetting c = new TicketCategorySetting(); c.setOrganisation(organisation(org)); c.setCode(s.name()); c.setLabel(SYSTEM_LABELS.get(s)); c.setSystem(true); c.setActive(true); c.setSortOrder(sort);
    saved.add(categories.save(c));
   }
  }
  saved.sort(Comparator.comparingInt(TicketCategorySetting::getSortOrder).thenComparing(c -> c.getId() == null ? Long.MAX_VALUE : c.getId()));
  return saved;
 }
 private Organisation organisation(Long org) { AppUser a = settings.current(); return a.getOrganisation(); }
 private CategoryDto category(TicketCategorySetting c, Long org) { return new CategoryDto(c.getId(), c.getCode(), c.getLabel(), c.isSystem(), c.isActive(), tickets.countByOrganisation_IdAndCategory(org, c.getCode())); }
 private CustomStatusDto status(TicketCustomStatus s) { return new CustomStatusDto(s.getId(), s.getCode(), s.getLabel(), s.getDescription(), s.getColor(), s.getBaseStatus(), s.getId() == null ? 0 : tickets.countByCustomStatus_Id(s.getId())); }
 private void apply(TicketCustomStatus s, CustomStatusRequest r) {
  if (!COLORS.contains(r.color())) throw bad("Unknown colour " + r.color() + ".");
  if (r.baseStatus() == null) throw bad("Choose the lifecycle status this status belongs to.");
  s.setLabel(r.label().trim()); s.setDescription(r.description() == null || r.description().isBlank() ? null : r.description().trim()); s.setColor(r.color()); s.setBaseStatus(r.baseStatus());
 }
 private static boolean isSystemCode(String code) { return Arrays.stream(TicketCategory.values()).anyMatch(s -> s.name().equalsIgnoreCase(code)); }
 private static boolean isSystemStatus(String code) { return Arrays.stream(TicketStatus.values()).anyMatch(s -> s.name().equalsIgnoreCase(code)); }
 private static String code(String value, int max) {
  String c = Objects.toString(value, "").trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "");
  if (c.isEmpty()) throw bad("A code is required.");
  return c.length() > max ? c.substring(0, max) : c;
 }
 private static Long orgOf(AppUser a) { if (a.getOrganisation() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No organisation."); return a.getOrganisation().getId(); }
 private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
 private static ResponseStatusException conflict(String m) { return new ResponseStatusException(HttpStatus.CONFLICT, m); }
 private static ResponseStatusException notFound(String m) { return new ResponseStatusException(HttpStatus.NOT_FOUND, m); }
}
