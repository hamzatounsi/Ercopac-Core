package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.ticketing.domain.*;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.repository.*;
import com.ercopac.ercopac_tracker.user.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// Ticketing settings of the current organisation. Everyone in the Ticketing app reads them (labels, SLA targets);
// only support leads (H24_LEAD, SALES_MANAGER_LEAD) change them.
@Service @Transactional
public class TicketSettingsService {
 public static final List<Role> SUPPORT_ROLES = List.of(Role.SALES_MANAGER_LEAD, Role.H24, Role.H24_LEAD);
 public static final List<Role> LEAD_ROLES = List.of(Role.SALES_MANAGER_LEAD, Role.H24_LEAD);
 public static final Set<String> LANGUAGES = Set.of("en", "fr", "es", "it", "pt", "de", "ar");
 private static final Set<String> COLORS = Set.of("blue", "amber", "red", "green", "gray", "purple", "coral");
 static final List<TicketStatus> DONE = List.of(TicketStatus.RESOLVED, TicketStatus.CLOSED, TicketStatus.CANCELLED);

 private static final Map<TicketStatus, StatusSettingDto> DEFAULT_STATUSES = new LinkedHashMap<>();
 private static final Map<TicketPriority, PrioritySettingDto> DEFAULT_PRIORITIES = new LinkedHashMap<>();
 static {
  for (StatusSettingDto s : List.of(
    new StatusSettingDto(TicketStatus.OPEN, "Open", "Waiting for a first response; the response SLA is running", "blue"),
    new StatusSettingDto(TicketStatus.IN_PROGRESS, "In progress", "An H24 agent is working on a fix", "amber"),
    new StatusSettingDto(TicketStatus.ESCALATED, "Escalated L2", "Handed over to an L2 technician", "red"),
    new StatusSettingDto(TicketStatus.RESOLVED, "Resolved", "Solution delivered, waiting for the customer", "green"),
    new StatusSettingDto(TicketStatus.CLOSED, "Closed", "No further action", "gray"),
    new StatusSettingDto(TicketStatus.REOPENED, "Reopened", "The customer reported the problem again", "purple"),
    new StatusSettingDto(TicketStatus.CANCELLED, "Cancelled", "Withdrawn, no action needed", "gray")))
   DEFAULT_STATUSES.put(s.status(), s);
  for (PrioritySettingDto p : List.of(
    new PrioritySettingDto(TicketPriority.CRITICAL, "Critical", 30, 4),
    new PrioritySettingDto(TicketPriority.HIGH, "High", 60, 8),
    new PrioritySettingDto(TicketPriority.MEDIUM, "Medium", 240, 24),
    new PrioritySettingDto(TicketPriority.LOW, "Low", 1440, 72)))
   DEFAULT_PRIORITIES.put(p.priority(), p);
 }

 private final TicketStatusSettingRepository statusSettings; private final TicketPrioritySettingRepository prioritySettings; private final TicketAgentProfileRepository profiles;
 private final TicketCustomerContactRepository contacts; private final TicketRepository tickets; private final UserRepository users; private final OrganisationRepository organisations; private final SecurityUtils security;
 public TicketSettingsService(TicketStatusSettingRepository statusSettings, TicketPrioritySettingRepository prioritySettings, TicketAgentProfileRepository profiles, TicketCustomerContactRepository contacts, TicketRepository tickets, UserRepository users, OrganisationRepository organisations, SecurityUtils security) {
  this.statusSettings = statusSettings; this.prioritySettings = prioritySettings; this.profiles = profiles; this.contacts = contacts; this.tickets = tickets; this.users = users; this.organisations = organisations; this.security = security;
 }

 public TicketSettingsDto settings() { Long org = organisationId(); return new TicketSettingsDto(statuses(org), new ArrayList<>(priorities(org).values())); }

 public TicketSettingsDto saveStatuses(List<StatusSettingDto> request) {
  AppUser lead = requireLead(); Long org = lead.getOrganisation().getId();
  Map<TicketStatus, TicketStatusSetting> saved = statusSettings.findByOrganisation_Id(org).stream().collect(Collectors.toMap(TicketStatusSetting::getStatus, Function.identity()));
  for (StatusSettingDto dto : request) {
   if (dto.status() == null || blank(dto.label())) throw bad("Every status needs a label.");
   if (!COLORS.contains(dto.color())) throw bad("Unknown colour " + dto.color() + ".");
   TicketStatusSetting s = saved.computeIfAbsent(dto.status(), k -> { TicketStatusSetting n = new TicketStatusSetting(); n.setOrganisation(lead.getOrganisation()); n.setStatus(k); return n; });
   s.setLabel(dto.label().trim()); s.setDescription(trimToNull(dto.description())); s.setColor(dto.color());
   statusSettings.save(s);
  }
  return settings();
 }

 public TicketSettingsDto savePriorities(List<PrioritySettingDto> request) {
  AppUser lead = requireLead(); Long org = lead.getOrganisation().getId();
  Map<TicketPriority, TicketPrioritySetting> saved = prioritySettings.findByOrganisation_Id(org).stream().collect(Collectors.toMap(TicketPrioritySetting::getPriority, Function.identity()));
  for (PrioritySettingDto dto : request) {
   if (dto.priority() == null || blank(dto.label())) throw bad("Every priority needs a label.");
   if (dto.firstResponseMinutes() < 1 || dto.resolutionHours() < 1) throw bad("SLA targets must be positive.");
   if (dto.firstResponseMinutes() > dto.resolutionHours() * 60) throw bad("The first response target of " + dto.label() + " is longer than its resolution target.");
   TicketPrioritySetting p = saved.computeIfAbsent(dto.priority(), k -> { TicketPrioritySetting n = new TicketPrioritySetting(); n.setOrganisation(lead.getOrganisation()); n.setPriority(k); return n; });
   p.setLabel(dto.label().trim()); p.setFirstResponseMinutes(dto.firstResponseMinutes()); p.setResolutionHours(dto.resolutionHours());
   prioritySettings.save(p);
  }
  return settings();
 }

 /** SLA targets per priority, defaults filled in for priorities the organisation has not configured. */
 public Map<TicketPriority, PrioritySettingDto> priorities(Long org) {
  Map<TicketPriority, PrioritySettingDto> result = new LinkedHashMap<>(DEFAULT_PRIORITIES);
  for (TicketPrioritySetting p : prioritySettings.findByOrganisation_Id(org)) result.put(p.getPriority(), new PrioritySettingDto(p.getPriority(), p.getLabel(), p.getFirstResponseMinutes(), p.getResolutionHours()));
  return result;
 }

 private List<StatusSettingDto> statuses(Long org) {
  Map<TicketStatus, StatusSettingDto> result = new LinkedHashMap<>(DEFAULT_STATUSES);
  for (TicketStatusSetting s : statusSettings.findByOrganisation_Id(org)) result.put(s.getStatus(), new StatusSettingDto(s.getStatus(), s.getLabel(), s.getDescription(), s.getColor()));
  return new ArrayList<>(result.values());
 }

 /** Support staff of the organisation with their ticketing profile and current load. */
 public List<AgentDto> agents() {
  AppUser actor = requireStaff(); Long org = actor.getOrganisation().getId();
  Map<Long, TicketAgentProfile> byUser = profiles.findByOrganisation_Id(org).stream().collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity()));
  List<Ticket> active = tickets.findActive(org, DONE);
  Map<Long, Long> open = active.stream().filter(t -> t.getAssignedSalesManager() != null).collect(Collectors.groupingBy(t -> t.getAssignedSalesManager().getId(), Collectors.counting()));
  Map<Long, Long> l2Open = active.stream().filter(t -> t.getL2Technician() != null).collect(Collectors.groupingBy(t -> t.getL2Technician().getId(), Collectors.counting()));
  return users.findByOrganisation_IdAndRoleInAndActiveTrueOrderByFullNameAsc(org, SUPPORT_ROLES).stream().distinct().map(u -> {
   TicketAgentProfile p = byUser.get(u.getId());
   return new AgentDto(u.getId(), u.getFullName(), u.getEmail(), u.getRoles().stream().map(Enum::name).sorted().toList(), u.hasAnyRole(LEAD_ROLES),
     p != null && p.isL2Technician(), p == null ? null : p.getSpecialization(), p == null ? null : p.getPreferredLanguage(),
     open.getOrDefault(u.getId(), 0L), l2Open.getOrDefault(u.getId(), 0L), u.isActive());
  }).toList();
 }

 public AgentDto updateAgent(Long userId, AgentProfileRequest request) {
  AppUser lead = requireLead(); Long org = lead.getOrganisation().getId();
  AppUser user = users.findByIdAndOrganisation_Id(userId, org).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
  if (!user.hasAnyRole(SUPPORT_ROLES)) throw bad("Only support staff have a ticketing profile.");
  TicketAgentProfile p = profile(user);
  p.setL2Technician(request.l2Technician()); p.setSpecialization(trimToNull(request.specialization())); p.setPreferredLanguage(language(request.preferredLanguage()));
  profiles.save(p);
  return agents().stream().filter(a -> a.id().equals(userId)).findFirst().orElseThrow();
 }

 /** Users that can be set as L2 technician on a ticket. */
 public boolean isL2Technician(AppUser user) { return user.hasAnyRole(SUPPORT_ROLES) && profiles.findByUser_Id(user.getId()).map(TicketAgentProfile::isL2Technician).orElse(false); }
 public Map<Long, TicketAgentProfile> profilesByUser(Long org) { return profiles.findByOrganisation_Id(org).stream().collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity())); }

 public MySettingsDto me() {
  AppUser me = current();
  boolean staff = me.hasAnyRole(SUPPORT_ROLES);
  Optional<TicketCustomerContact> contact = contacts.findFirstByPortalUser_Id(me.getId());
  String language = staff ? profiles.findByUser_Id(me.getId()).map(TicketAgentProfile::getPreferredLanguage).orElse(null) : contact.map(TicketCustomerContact::getLanguage).orElse(null);
  return new MySettingsDto(me.getId(), me.getFullName(), language == null ? "en" : language, me.hasAnyRole(LEAD_ROLES), staff, contact.map(c -> c.getCustomer().getId()).orElse(null));
 }

 /** Language the current user writes in: stored on the agent profile (staff) or on the linked customer contact (clients). */
 public MySettingsDto updateMyLanguage(LanguageRequest request) {
  AppUser me = current(); String language = language(request.language());
  if (me.hasAnyRole(SUPPORT_ROLES)) { TicketAgentProfile p = profile(me); p.setPreferredLanguage(language); profiles.save(p); }
  else { TicketCustomerContact c = contacts.findFirstByPortalUser_Id(me.getId()).orElseThrow(() -> bad("Your account is not linked to a customer contact yet; ask the support team to link it.")); c.setLanguage(language); contacts.save(c); }
  return me();
 }

 /** Language of a user for translation: agent profile language, or the language of the customer contact linked to a client. */
 public String languageOf(AppUser user) {
  if (user == null) return "en";
  String lang = user.hasAnyRole(SUPPORT_ROLES) ? profiles.findByUser_Id(user.getId()).map(TicketAgentProfile::getPreferredLanguage).orElse(null)
    : contacts.findFirstByPortalUser_Id(user.getId()).map(TicketCustomerContact::getLanguage).orElse(null);
  return lang == null ? "en" : lang;
 }

 private TicketAgentProfile profile(AppUser user) {
  return profiles.findByUser_Id(user.getId()).orElseGet(() -> { TicketAgentProfile n = new TicketAgentProfile(); n.setUser(user); n.setOrganisation(user.getOrganisation()); return n; });
 }
 private String language(String code) { if (blank(code)) return null; String c = code.trim().toLowerCase(); if (!LANGUAGES.contains(c)) throw bad("Unsupported language " + code + "."); return c; }
 private Long organisationId() { AppUser a = current(); if (a.getOrganisation() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No organisation."); return a.getOrganisation().getId(); }
 AppUser current() { return users.findById(security.getCurrentUserId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current account no longer exists.")); }
 AppUser requireStaff() { AppUser a = current(); if (!a.hasAnyRole(SUPPORT_ROLES) || a.getOrganisation() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the support team can do this."); return a; }
 AppUser requireLead() { AppUser a = current(); if (!a.hasAnyRole(LEAD_ROLES) || a.getOrganisation() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only support leads can change ticketing settings."); return a; }
 private static boolean blank(String v) { return v == null || v.trim().isEmpty(); }
 private static String trimToNull(String v) { return blank(v) ? null : v.trim(); }
 private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
}
