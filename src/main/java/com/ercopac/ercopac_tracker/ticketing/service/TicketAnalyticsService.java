package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.ticketing.domain.*;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.repository.*;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Performance dashboard figures for the tickets created in a period.
// First response = first public message on the ticket from someone other than its client.
// Resolution = resolvedAt (or closedAt) minus createdAt.
// A ticket meets its SLA when neither the first response nor the resolution took longer than the targets of its priority;
// unfinished tickets are measured up to now. Cancelled tickets are left out of SLA figures.
@Service @Transactional
public class TicketAnalyticsService {
 private final TicketRepository tickets; private final TicketMessageRepository messages; private final UserRepository users; private final TicketSettingsService settings;
 public TicketAnalyticsService(TicketRepository tickets, TicketMessageRepository messages, UserRepository users, TicketSettingsService settings) { this.tickets = tickets; this.messages = messages; this.users = users; this.settings = settings; }

 public TicketAnalytics analytics(Instant from, Instant to, Long customerId) { return analytics(from, to, customerId, null, null); }

 /** Same figures restricted to tickets about equipment of a catalogue type and/or whose serial number contains the given text. */
 public TicketAnalytics analytics(Instant from, Instant to, Long customerId, Long equipmentTypeId, String serial) {
  AppUser lead = settings.requireLead(); Long org = lead.getOrganisation().getId();
  Instant end = to == null ? Instant.now() : to;
  Instant start = from == null ? end.minus(30, ChronoUnit.DAYS) : from;
  String serialFilter = serial == null || serial.isBlank() ? null : serial.trim().toLowerCase(Locale.ROOT);
  boolean equipmentFilter = equipmentTypeId != null || serialFilter != null;
  List<Ticket> list = tickets.findForAnalytics(org, start, end, customerId).stream().filter(t -> !equipmentFilter || matchesEquipment(t.getEquipment(), equipmentTypeId, serialFilter)).toList();
  Map<TicketPriority, PrioritySettingDto> sla = settings.priorities(org);
  Map<Long, Instant> firstResponse = firstResponses(list);
  Instant now = Instant.now();
  List<Measure> measures = list.stream().map(t -> measure(t, firstResponse.get(t.getId()), sla.get(t.getPriority()), now)).toList();

  List<Breakdown> byStatus = Arrays.stream(TicketStatus.values()).map(s -> new Breakdown(s.name(), measures.stream().filter(m -> m.ticket.getStatus() == s).count())).toList();
  List<Breakdown> byPriority = Arrays.stream(TicketPriority.values()).map(p -> new Breakdown(p.name(), measures.stream().filter(m -> m.ticket.getPriority() == p).count())).toList();

  Map<Long, List<Measure>> perCustomer = measures.stream().filter(m -> m.ticket.getCustomer() != null).collect(Collectors.groupingBy(m -> m.ticket.getCustomer().getId(), LinkedHashMap::new, Collectors.toList()));
  List<CustomerStat> byCustomer = new ArrayList<>(perCustomer.values().stream().map(ms -> new CustomerStat(ms.get(0).ticket.getCustomer().getId(), ms.get(0).ticket.getCustomer().getName(), ms.size(), ms.stream().filter(m -> !m.done).count(), compliance(ms))).toList());
  List<Measure> noCustomer = measures.stream().filter(m -> m.ticket.getCustomer() == null).toList();
  if (!noCustomer.isEmpty()) byCustomer.add(new CustomerStat(null, "No customer", noCustomer.size(), noCustomer.stream().filter(m -> !m.done).count(), compliance(noCustomer)));
  byCustomer.sort(Comparator.comparingLong(CustomerStat::total).reversed());

  // One bucket per day for up to two months, per month beyond that
  boolean daily = Duration.between(start, end).toDays() <= 62;
  DateTimeFormatter key = DateTimeFormatter.ofPattern(daily ? "yyyy-MM-dd" : "yyyy-MM").withZone(ZoneOffset.UTC);
  Map<String, Long> buckets = new LinkedHashMap<>();
  LocalDate day = LocalDate.ofInstant(start, ZoneOffset.UTC), last = LocalDate.ofInstant(end, ZoneOffset.UTC);
  if (!daily) day = day.withDayOfMonth(1);
  for (int guard = 0; !day.isAfter(last) && guard < 400; guard++, day = daily ? day.plusDays(1) : day.plusMonths(1)) buckets.put(key.format(day.atStartOfDay(ZoneOffset.UTC)), 0L);
  measures.forEach(m -> buckets.merge(key.format(m.ticket.getCreatedAt()), 1L, Long::sum));
  List<Breakdown> timeline = buckets.entrySet().stream().map(e -> new Breakdown(e.getKey(), e.getValue())).toList();

  Map<Long, TicketAgentProfile> profiles = settings.profilesByUser(org);
  List<AgentStat> agents = users.findByOrganisation_IdAndRoleInAndActiveTrueOrderByFullNameAsc(org, TicketSettingsService.SUPPORT_ROLES).stream().distinct().map(u -> {
   Predicate<Ticket> mine = t -> (t.getAssignedSalesManager() != null && t.getAssignedSalesManager().getId().equals(u.getId())) || (t.getL2Technician() != null && t.getL2Technician().getId().equals(u.getId()));
   List<Measure> ms = measures.stream().filter(m -> mine.test(m.ticket)).toList();
   TicketAgentProfile p = profiles.get(u.getId());
   return new AgentStat(u.getId(), u.getFullName(), p == null ? null : p.getSpecialization(), p != null && p.isL2Technician(), ms.size(), ms.stream().filter(m -> !m.done).count(),
     ms.stream().filter(m -> m.resolved).count(), ms.stream().filter(m -> m.escalated).count(), average(ms.stream().map(m -> m.responseHours)), compliance(ms));
  }).toList();

  AnalyticsTotals totals = new AnalyticsTotals(measures.size(), measures.stream().filter(m -> !m.done).count(), measures.stream().filter(m -> m.resolved).count(),
    measures.stream().filter(m -> m.escalated).count(), measures.stream().filter(m -> m.overdue).count(),
    average(measures.stream().map(m -> m.responseHours)), average(measures.stream().map(m -> m.resolutionHours)), compliance(measures));
  List<EquipmentTicket> equipmentTickets = !equipmentFilter ? List.of() : list.stream().sorted(Comparator.comparing(Ticket::getCreatedAt).reversed()).map(t -> new EquipmentTicket(t.getId(), t.getTicketNumber(), t.getSubject(),
    t.getCustomer() == null ? null : t.getCustomer().getName(), t.getSite(), t.getEquipment().getName(), t.getEquipment().getSerialNumber(), t.getStatus())).toList();
  return new TicketAnalytics(start, end, daily ? "day" : "month", totals, byStatus, byPriority, byCustomer, timeline, agents, equipmentTickets);
 }

 static boolean matchesEquipment(TicketCustomerEquipment e, Long typeId, String serial) {
  if (e == null) return false;
  if (typeId != null && (e.getEquipmentType() == null || !typeId.equals(e.getEquipmentType().getId()))) return false;
  return serial == null || (e.getSerialNumber() != null && e.getSerialNumber().toLowerCase(Locale.ROOT).contains(serial));
 }

 /** Overdue tickets and average resolution time of a ticket list (the Tickets page counters). */
 public double[] overdueAndAverageResolution(List<Ticket> list, Long org) {
  Map<TicketPriority, PrioritySettingDto> sla = settings.priorities(org); Instant now = Instant.now();
  List<Measure> ms = list.stream().map(t -> measure(t, null, sla.get(t.getPriority()), now)).toList();
  Double avg = average(ms.stream().map(m -> m.resolutionHours));
  return new double[]{ ms.stream().filter(m -> m.overdue).count(), avg == null ? 0 : avg };
 }

 /** Unfinished ticket older than the resolution target of its priority. */
 public static boolean overdue(Ticket t, PrioritySettingDto target, Instant now) {
  return !TicketSettingsService.DONE.contains(t.getStatus()) && target != null && Duration.between(t.getCreatedAt(), now).toMinutes() > target.resolutionHours() * 60L;
 }

 static Measure measure(Ticket t, Instant firstResponse, PrioritySettingDto target, Instant now) {
  boolean done = TicketSettingsService.DONE.contains(t.getStatus());
  Instant finished = t.getResolvedAt() != null ? t.getResolvedAt() : t.getClosedAt() != null ? t.getClosedAt() : done ? t.getUpdatedAt() : null;
  Instant responseEnd = firstResponse != null ? firstResponse : finished != null ? finished : now;
  Instant resolutionEnd = finished != null ? finished : now;
  boolean breached = target != null && (minutes(t.getCreatedAt(), responseEnd) > target.firstResponseMinutes() || minutes(t.getCreatedAt(), resolutionEnd) > target.resolutionHours() * 60L);
  return new Measure(t, done, t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED,
    t.getStatus() == TicketStatus.ESCALATED || t.getEscalationLevel() > 0 || t.getL2Technician() != null,
    overdue(t, target, now), t.getStatus() == TicketStatus.CANCELLED ? null : !breached,
    firstResponse == null ? null : hours(t.getCreatedAt(), firstResponse), finished == null || t.getStatus() == TicketStatus.CANCELLED ? null : hours(t.getCreatedAt(), finished));
 }

 /** Share of tickets that met their SLA, in percent; null when there is nothing to measure. */
 static Double compliance(List<Measure> ms) {
  List<Boolean> met = ms.stream().map(m -> m.slaMet).filter(Objects::nonNull).toList();
  return met.isEmpty() ? null : Math.round(1000.0 * met.stream().filter(b -> b).count() / met.size()) / 10.0;
 }
 private static Double average(java.util.stream.Stream<Double> values) { OptionalDouble avg = values.filter(Objects::nonNull).mapToDouble(Double::doubleValue).average(); return avg.isPresent() ? Math.round(avg.getAsDouble() * 10) / 10.0 : null; }
 private static long minutes(Instant a, Instant b) { return Duration.between(a, b).toMinutes(); }
 private static double hours(Instant a, Instant b) { return Math.max(0, Duration.between(a, b).toSeconds()) / 3600.0; }
 private Map<Long, Instant> firstResponses(List<Ticket> list) {
  Map<Long, Instant> result = new HashMap<>();
  if (list.isEmpty()) return result;
  for (Object[] row : messages.firstResponses(list.stream().map(Ticket::getId).toList())) result.put((Long) row[0], (Instant) row[1]);
  return result;
 }

 record Measure(Ticket ticket, boolean done, boolean resolved, boolean escalated, boolean overdue, Boolean slaMet, Double responseHours, Double resolutionHours) {}
}
