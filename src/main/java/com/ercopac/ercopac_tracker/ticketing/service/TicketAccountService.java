package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.org_admin.dto.OrgAdminDtos;
import com.ercopac.ercopac_tracker.org_admin.service.OrganisationAdminService;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

// Accounts for new support employees created by support leads from Ticketing settings. Reuses the organisation
// user creation (licence capacity, password policy, unique email, tenant = the lead's organisation).
@Service @Transactional
public class TicketAccountService {
 private final OrganisationAdminService users; private final TicketSettingsService settings; private final com.ercopac.ercopac_tracker.department.repository.DepartmentRepository departments;
 public TicketAccountService(OrganisationAdminService users, TicketSettingsService settings, com.ercopac.ercopac_tracker.department.repository.DepartmentRepository departments) { this.users = users; this.settings = settings; this.departments = departments; }

 /** Departments of the lead's organisation (technician accounts need one). */
 public List<Ref> departments() {
  Long org = settings.requireLead().getOrganisation().getId();
  return departments.findByOrganisation_IdOrderByCodeAsc(org).stream().map(d -> new Ref(d.getId(), d.getLabel() == null || d.getLabel().isBlank() ? d.getCode() : d.getCode() + " - " + d.getLabel())).toList();
 }

 /** kind: H24 (agent), H24_LEAD (lead) or TECHNICIAN (employee with a resource type, flagged as L2 technician). */
 public AgentDto create(AccountRequest r) {
  settings.requireLead();
  String kind = r.kind().trim().toUpperCase(Locale.ROOT);
  Set<String> roles = switch (kind) {
   case "H24" -> Set.of("H24");
   case "H24_LEAD" -> Set.of("H24_LEAD");
   // TODO(confirm with Anis): technicians are created as EMPLOYEE (resource planning profile: department + resource type) and flagged L2
   case "TECHNICIAN" -> Set.of("EMPLOYEE");
   default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown account type " + r.kind() + ".");
  };
  if ("TECHNICIAN".equals(kind) && (r.departmentId() == null || r.resourceTypeId() == null))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A technician needs a department and a resource type.");
  OrgAdminDtos.UserSummary created = users.createUser(new OrgAdminDtos.CreateUserRequest(r.fullName(), r.email(), r.password(), roles,
    "TECHNICIAN".equals(kind) ? r.departmentId() : null, "TECHNICIAN".equals(kind) ? r.resourceTypeId() : null, null, null, true));
  return settings.updateAgent(created.id(), new AgentProfileRequest("TECHNICIAN".equals(kind), r.specialization(), null));
 }
}
