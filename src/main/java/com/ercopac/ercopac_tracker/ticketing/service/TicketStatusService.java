package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatus;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketRepository;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketStatusRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Ticket statuses of the current organisation (Ticketing settings > Statuses).
 * Everyone in the Ticketing app reads them; only support leads change them.
 * Rules: names are unique (case-insensitive); exactly one default status, which is active and not closed;
 * a status used by tickets is never hard deleted (its tickets are reassigned first, or it is deactivated).
 * There are no built-in statuses: an organisation only has the statuses its leads create (plus, after the upgrade,
 * the former enum statuses its existing tickets still used, see TicketStatusMigration). The first active, non-closed
 * status becomes the default automatically; while an organisation has no status, new tickets have none.
 */
@Service @Transactional
public class TicketStatusService {
 public static final Set<String> PALETTE = Set.of("blue", "amber", "red", "green", "gray", "purple", "coral");
 private static final Pattern HEX = Pattern.compile("^#[0-9a-fA-F]{6}$");

 /** Former enum statuses (code, name, description, colour, default, closed): only used to migrate tickets that still use them. */
 public record Seed(String code, String name, String description, String color, boolean isDefault, boolean closed) {}
 public static final List<Seed> STANDARD = List.of(
   new Seed("OPEN", "Open", "Waiting for a first response; the response SLA is running", "blue", true, false),
   new Seed("IN_PROGRESS", "In progress", "An H24 agent is working on a fix", "amber", false, false),
   new Seed("ESCALATED", "Escalated L2", "Handed over to an L2 technician", "red", false, false),
   new Seed("REOPENED", "Reopened", "The customer reported the problem again", "purple", false, false),
   new Seed("RESOLVED", "Resolved", "Solution delivered, waiting for the customer", "green", false, true),
   new Seed("CLOSED", "Closed", "No further action", "gray", false, true),
   new Seed("CANCELLED", "Cancelled", "Withdrawn, no action needed", "gray", false, true));
 // Codes of standard statuses the quick actions (escalate, resolve, close, reopen) prefer when they exist and are active
 public static final String ESCALATED = "ESCALATED", RESOLVED = "RESOLVED", CLOSED = "CLOSED", REOPENED = "REOPENED";

 private final TicketStatusRepository statuses; private final TicketRepository tickets; private final TicketSettingsService settings;
 public TicketStatusService(TicketStatusRepository statuses, TicketRepository tickets, TicketSettingsService settings) {
  this.statuses = statuses; this.tickets = tickets; this.settings = settings;
 }

 // ---------- reading ----------
 /** Every status of the current user's organisation in display order; activeOnly keeps the ones offered in dropdowns. */
 public List<TicketStatusDto> list(boolean activeOnly) {
  AppUser me = settings.current(); Organisation org = orgOf(me);
  return statusesOf(org).stream().filter(s -> !activeOnly || s.isActive()).map(this::dto).toList();
 }

 /** Statuses of an organisation in display order. */
 public List<TicketStatus> statusesOf(Organisation org) { return statuses.findByOrganisation_IdOrderByDisplayOrderAscIdAsc(org.getId()); }

 /** Development fixtures only: the former enum status with this code, created when missing. */
 public TicketStatus legacy(Organisation org, String code) {
  return statuses.findByOrganisation_IdAndCodeIgnoreCase(org.getId(), code).orElseGet(() -> {
   Seed seed = STANDARD.stream().filter(x -> x.code().equals(code)).findFirst().orElseThrow();
   TicketStatus s = new TicketStatus(); s.setOrganisation(org); s.setCode(seed.code()); s.setName(seed.name()); s.setDescription(seed.description());
   s.setColor(seed.color()); s.setClosed(seed.closed()); s.setActive(true); s.setDisplayOrder(STANDARD.indexOf(seed));
   s.setDefault(seed.isDefault() && statuses.findFirstByOrganisation_IdAndIsDefaultTrue(org.getId()).isEmpty());
   return statuses.save(s);
  });
 }

 /** Status given to new tickets: the default one, else the first active open status; null while the organisation has none. */
 public TicketStatus defaultStatus(Organisation org) {
  return statuses.findFirstByOrganisation_IdAndIsDefaultTrue(org.getId())
    .or(() -> statusesOf(org).stream().filter(s -> s.isActive() && !s.isClosed()).findFirst()).orElse(null);
 }

 /** Active status of the organisation a ticket may be moved to, by id or (fallback) by code. */
 public TicketStatus requireAssignable(Organisation org, Long id, String code) {
  Optional<TicketStatus> found = id != null ? statuses.findByIdAndOrganisation_Id(id, org.getId())
    : code != null && !code.isBlank() ? statuses.findByOrganisation_IdAndCodeIgnoreCase(org.getId(), code.trim()) : Optional.empty();
  if (id == null && (code == null || code.isBlank())) throw bad("A status is required.");
  TicketStatus s = found.orElseThrow(() -> bad("Unknown ticket status."));
  if (!s.isActive()) throw bad("The status " + s.getName() + " is inactive.");
  return s;
 }

 /** Active status with a standard code, if the organisation kept it. */
 public Optional<TicketStatus> activeByCode(Organisation org, String code) {
  return statusesOf(org).stream().filter(s -> s.isActive() && s.getCode().equalsIgnoreCase(code)).findFirst();
 }
 /** Target of "Resolve": the RESOLVED status, else the first active closed status. */
 public TicketStatus resolveTarget(Organisation org) { return activeByCode(org, RESOLVED).filter(TicketStatus::isClosed).orElseGet(() -> firstClosed(org, false)); }
 /** Target of "Close": the CLOSED status, else the last active closed status. */
 public TicketStatus closeTarget(Organisation org) { return activeByCode(org, CLOSED).filter(TicketStatus::isClosed).orElseGet(() -> firstClosed(org, true)); }
 /** Target of "Reopen": the REOPENED status, else the default status. */
 public TicketStatus reopenTarget(Organisation org) {
  TicketStatus target = activeByCode(org, REOPENED).filter(s -> !s.isClosed()).orElseGet(() -> defaultStatus(org));
  if (target == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "No active open status is configured in Ticketing settings.");
  return target;
 }
 private TicketStatus firstClosed(Organisation org, boolean last) {
  List<TicketStatus> closed = statusesOf(org).stream().filter(s -> s.isActive() && s.isClosed()).toList();
  if (closed.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT, "No active closed status is configured in Ticketing settings.");
  return last ? closed.get(closed.size() - 1) : closed.get(0);
 }

 // ---------- changes (support leads) ----------
 public TicketStatusDto create(TicketStatusRequest r) {
  AppUser lead = settings.requireLead(); Organisation org = orgOf(lead);
  List<TicketStatus> all = statusesOf(org);
  String name = r.name().trim();
  uniqueName(all, name, null);
  String code = uniqueCode(org, code(blank(r.code()) ? name : r.code()));
  TicketStatus s = new TicketStatus(); s.setOrganisation(org); s.setCode(code);
  s.setDisplayOrder(r.displayOrder() != null ? r.displayOrder() : all.stream().mapToInt(TicketStatus::getDisplayOrder).max().orElse(-1) + 1);
  s.setActive(r.active() == null || r.active());
  apply(s, r, name);
  s.setDefault(false);
  s = statuses.save(s);
  // Explicitly requested, or the organisation has no default yet and this status can be one
  boolean noDefault = all.stream().noneMatch(TicketStatus::isDefault);
  if (Boolean.TRUE.equals(r.isDefault()) || (noDefault && s.isActive() && !s.isClosed())) makeDefault(org, s);
  return dto(s);
 }

 public TicketStatusDto update(Long id, TicketStatusRequest r) {
  AppUser lead = settings.requireLead(); Organisation org = orgOf(lead);
  List<TicketStatus> all = statusesOf(org);
  TicketStatus s = find(org, id);
  String name = r.name().trim();
  uniqueName(all, name, id);
  if (r.active() != null && !r.active() && s.isDefault()) throw conflict("The default status cannot be deactivated. Make another status the default first.");
  if (r.isDefault() != null && !r.isDefault() && s.isDefault()) throw conflict("There must be a default status. Make another status the default instead.");
  apply(s, r, name);
  if (s.isDefault() && s.isClosed()) throw bad("The default status cannot be a closed status.");
  if (r.active() != null) s.setActive(r.active());
  if (r.displayOrder() != null) s.setDisplayOrder(r.displayOrder());
  s = statuses.save(s);
  if (Boolean.TRUE.equals(r.isDefault()) && !s.isDefault()) makeDefault(org, s);
  return dto(s);
 }

 /** New display order: the ids of the organisation statuses, first to last. */
 public List<TicketStatusDto> reorder(List<Long> ids) {
  AppUser lead = settings.requireLead(); Organisation org = orgOf(lead);
  Map<Long, TicketStatus> byId = new HashMap<>(); statusesOf(org).forEach(s -> byId.put(s.getId(), s));
  int order = 0;
  for (Long id : ids) { TicketStatus s = byId.remove(id); if (s == null) throw bad("Unknown ticket status " + id + "."); s.setDisplayOrder(order++); }
  for (TicketStatus rest : byId.values()) rest.setDisplayOrder(order++);
  return list(false);
 }

 /**
  * Deletes a status. When tickets use it, reassignTo (another active status id) is required and they are moved first;
  * without it the call fails with 409 and the ticket count, so the caller can reassign or deactivate instead.
  */
 public void delete(Long id, Long reassignTo) {
  AppUser lead = settings.requireLead(); Organisation org = orgOf(lead);
  List<TicketStatus> all = statusesOf(org);
  TicketStatus s = find(org, id);
  if (s.isDefault()) throw conflict("The default status cannot be deleted. Make another status the default first.");
  if (all.size() <= 1) throw conflict("The last ticket status cannot be deleted.");
  long used = tickets.countByStatus_Id(s.getId());
  if (used > 0) {
   if (reassignTo == null) throw new TicketStatusInUseException(used + " ticket(s) use the status " + s.getName() + ". Move them to another status or deactivate it instead.", used);
   if (reassignTo.equals(s.getId())) throw bad("Choose another status to move the tickets to.");
   TicketStatus target = find(org, reassignTo);
   if (!target.isActive()) throw bad("The status " + target.getName() + " is inactive.");
   tickets.reassignStatus(org.getId(), s.getId(), target.getId());
  }
  statuses.delete(s);
 }

 // ---------- helpers ----------
 private void makeDefault(Organisation org, TicketStatus s) {
  if (!s.isActive()) throw bad("An inactive status cannot be the default status.");
  if (s.isClosed()) throw bad("The default status cannot be a closed status.");
  for (TicketStatus other : statusesOf(org)) if (other.isDefault() && !other.getId().equals(s.getId())) { other.setDefault(false); statuses.save(other); }
  s.setDefault(true); statuses.save(s);
 }
 private void apply(TicketStatus s, TicketStatusRequest r, String name) {
  String color = r.color().trim();
  if (!PALETTE.contains(color) && !HEX.matcher(color).matches()) throw bad("Unknown colour " + color + ".");
  s.setName(name); s.setDescription(blank(r.description()) ? null : r.description().trim()); s.setColor(color);
  if (r.isClosed() != null) s.setClosed(r.isClosed());
 }
 private void uniqueName(List<TicketStatus> all, String name, Long except) {
  if (name.isEmpty()) throw bad("A status name is required.");
  if (all.stream().anyMatch(o -> !o.getId().equals(except) && o.getName().trim().equalsIgnoreCase(name))) throw conflict("A status named " + name + " already exists.");
 }
 private String uniqueCode(Organisation org, String base) {
  String code = base; int n = 2;
  while (statuses.existsByOrganisation_IdAndCodeIgnoreCase(org.getId(), code)) { String suffix = "_" + n++; code = (base.length() + suffix.length() > 40 ? base.substring(0, 40 - suffix.length()) : base) + suffix; }
  return code;
 }
 private TicketStatus find(Organisation org, Long id) { return statuses.findByIdAndOrganisation_Id(id, org.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found.")); }
 public TicketStatusDto dto(TicketStatus s) {
  return new TicketStatusDto(s.getId(), s.getCode(), s.getName(), s.getDescription(), s.getColor(), s.getDisplayOrder(), s.isDefault(), s.isClosed(), s.isActive(), s.getId() == null ? 0 : tickets.countByStatus_Id(s.getId()));
 }
 public static StatusRef ref(TicketStatus s) { return s == null ? null : new StatusRef(s.getId(), s.getCode(), s.getName(), s.getColor(), s.isClosed()); }
 private static String code(String value) {
  String c = Objects.toString(value, "").trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "");
  if (c.isEmpty()) c = "STATUS";
  return c.length() > 40 ? c.substring(0, 40) : c;
 }
 private static Organisation orgOf(AppUser a) { if (a.getOrganisation() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No organisation."); return a.getOrganisation(); }
 private static boolean blank(String v) { return v == null || v.isBlank(); }
 private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
 private static ResponseStatusException conflict(String m) { return new ResponseStatusException(HttpStatus.CONFLICT, m); }
}
