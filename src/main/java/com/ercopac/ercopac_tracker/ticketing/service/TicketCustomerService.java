package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.admin.domain.Customer;
import com.ercopac.ercopac_tracker.admin.repository.CustomerRepository;
import com.ercopac.ercopac_tracker.crm.domain.CrmEquipmentType;
import com.ercopac.ercopac_tracker.crm.repository.CrmEquipmentTypeRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.ticketing.domain.*;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.repository.*;
import com.ercopac.ercopac_tracker.user.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

// Customer companies of the organisation (the shared `customers` table, also used by Projectum projects)
// with the sites, contacts, installed equipment and projects the support team needs on tickets.
// Projects are read from Projectum; only projects missing from Projectum are entered by hand (TicketCustomerProject).
// Support staff read everything; leads create and change customers and their records.
@Service @Transactional
public class TicketCustomerService {
 private final CustomerRepository customers; private final TicketCustomerSiteRepository sites; private final TicketCustomerContactRepository contacts; private final TicketCustomerEquipmentRepository equipment;
 private final CrmEquipmentTypeRepository equipmentTypes; private final ProjectRepository projects; private final TicketRepository tickets; private final UserRepository users; private final TicketSettingsService settings; private final TicketCustomerProjectRepository manualProjects;
 public TicketCustomerService(CustomerRepository customers, TicketCustomerSiteRepository sites, TicketCustomerContactRepository contacts, TicketCustomerEquipmentRepository equipment, CrmEquipmentTypeRepository equipmentTypes, ProjectRepository projects, TicketRepository tickets, UserRepository users, TicketSettingsService settings, TicketCustomerProjectRepository manualProjects) {
  this.customers = customers; this.sites = sites; this.contacts = contacts; this.equipment = equipment; this.equipmentTypes = equipmentTypes; this.projects = projects; this.tickets = tickets; this.users = users; this.settings = settings; this.manualProjects = manualProjects;
 }

 // ---------- customers ----------
 public List<CustomerDto> list() { Long org = staffOrg(); List<Project> all = projects.findAllByOrganisationId(org); return customers.findByOrganisation_IdOrderByNameAsc(org).stream().map(c -> customer(c, all)).toList(); }
 public CustomerDto get(Long id) { Long org = staffOrg(); return customer(customer(id, org), projects.findAllByOrganisationId(org)); }

 public CustomerDto create(CustomerRequest r) {
  AppUser lead = settings.requireLead(); Long org = lead.getOrganisation().getId();
  if (customers.existsByOrganisation_IdAndCustomerCodeIgnoreCase(org, r.code().trim())) throw conflict("Customer code " + r.code().trim() + " is already used.");
  Customer c = new Customer(); c.setOrganisation(lead.getOrganisation()); apply(c, r);
  return customer(customers.save(c), projects.findAllByOrganisationId(org));
 }

 public CustomerDto update(Long id, CustomerRequest r) {
  Long org = leadOrg(); Customer c = customer(id, org);
  customers.findByOrganisation_IdAndCustomerCodeIgnoreCase(org, r.code().trim()).filter(o -> !o.getId().equals(id)).ifPresent(o -> { throw conflict("Customer code " + r.code().trim() + " is already used."); });
  apply(c, r);
  return customer(customers.save(c), projects.findAllByOrganisationId(org));
 }

 // ---------- sites ----------
 public List<SiteDto> sites(Long customerId) { Long org = staffOrg(); customer(customerId, org); return sitesOf(org, customerId); }
 public SiteDto createSite(Long customerId, SiteRequest r) { Long org = leadOrg(); TicketCustomerSite s = new TicketCustomerSite(); Customer c = customer(customerId, org); s.setOrganisation(c.getOrganisation()); s.setCustomer(c); s.setName(r.name().trim()); s.setAddress(trim(r.address())); return site(sites.save(s)); }
 public SiteDto updateSite(Long customerId, Long siteId, SiteRequest r) { TicketCustomerSite s = site(customerId, siteId, leadOrg()); s.setName(r.name().trim()); s.setAddress(trim(r.address())); return site(sites.save(s)); }
 public void deleteSite(Long customerId, Long siteId) {
  Long org = leadOrg(); TicketCustomerSite s = site(customerId, siteId, org);
  // Equipment stays registered at the customer, without a site
  equipment.findByOrganisation_IdAndCustomer_IdOrderByNameAsc(org, customerId).stream().filter(e -> e.getSite() != null && e.getSite().getId().equals(siteId)).forEach(e -> { e.setSite(null); equipment.save(e); });
  sites.delete(s);
 }

 // ---------- contacts ----------
 public List<ContactDto> contacts(Long customerId) { Long org = staffOrg(); customer(customerId, org); return contacts.findByOrganisation_IdAndCustomer_IdOrderByFullNameAsc(org, customerId).stream().map(this::contact).toList(); }
 public ContactDto createContact(Long customerId, ContactRequest r) { Long org = leadOrg(); Customer c = customer(customerId, org); TicketCustomerContact x = new TicketCustomerContact(); x.setOrganisation(c.getOrganisation()); x.setCustomer(c); apply(x, r, org); return contact(contacts.save(x)); }
 public ContactDto updateContact(Long customerId, Long contactId, ContactRequest r) { Long org = leadOrg(); TicketCustomerContact x = contact(customerId, contactId, org); apply(x, r, org); return contact(contacts.save(x)); }
 public void deleteContact(Long customerId, Long contactId) { contacts.delete(contact(customerId, contactId, leadOrg())); }

 /** CLIENT accounts of the organisation that a contact can be linked to. */
 public List<UserSummary> portalUsers() {
  Long org = staffOrg();
  return users.findByOrganisation_IdAndRoleInAndActiveTrueOrderByFullNameAsc(org, List.of(Role.CLIENT)).stream().distinct().map(this::user).toList();
 }

 // ---------- equipment ----------
 public List<EquipmentDto> equipment(Long customerId) { Long org = staffOrg(); customer(customerId, org); return equipmentOf(org, customerId); }
 public EquipmentDto createEquipment(Long customerId, EquipmentRequest r) { Long org = leadOrg(); Customer c = customer(customerId, org); TicketCustomerEquipment e = new TicketCustomerEquipment(); e.setOrganisation(c.getOrganisation()); e.setCustomer(c); apply(e, r, org, customerId); return equipment(equipment.save(e)); }
 public EquipmentDto updateEquipment(Long customerId, Long equipmentId, EquipmentRequest r) { Long org = leadOrg(); TicketCustomerEquipment e = equipment(customerId, equipmentId, org); apply(e, r, org, customerId); return equipment(equipment.save(e)); }
 public void deleteEquipment(Long customerId, Long equipmentId) {
  TicketCustomerEquipment e = equipment(customerId, equipmentId, leadOrg());
  if (tickets.existsByEquipment_Id(equipmentId)) throw conflict("This equipment is referenced by tickets and cannot be removed.");
  equipment.delete(e);
 }
 /** Equipment catalogue of the organisation (maintained in CRM settings). */
 public List<Ref> equipmentTypes() { Long org = staffOrg(); return equipmentTypes.findByOrganisation_IdAndActiveTrueOrderByNameAsc(org).stream().map(t -> new Ref(t.getId(), t.getName())).toList(); }

 // ---------- projects ----------
 /** Projects tab: the customer's Projectum projects followed by the ones entered by hand. */
 public List<ProjectDto> projects(Long customerId) {
  Long org = staffOrg(); customer(customerId, org);
  List<ProjectDto> result = new ArrayList<>(projectsOf(org, customerId));
  manualProjects.findByOrganisation_IdAndCustomer_IdOrderByNameAsc(org, customerId).stream().map(TicketCustomerService::project).forEach(result::add);
  return result;
 }
 public ProjectDto createManualProject(Long customerId, ManualProjectRequest r) {
  Long org = leadOrg(); Customer c = customer(customerId, org);
  TicketCustomerProject p = new TicketCustomerProject(); p.setOrganisation(c.getOrganisation()); p.setCustomer(c); apply(p, r);
  return project(manualProjects.save(p));
 }
 public ProjectDto updateManualProject(Long customerId, Long projectId, ManualProjectRequest r) { TicketCustomerProject p = manualProject(customerId, projectId, leadOrg()); apply(p, r); return project(manualProjects.save(p)); }
 public void deleteManualProject(Long customerId, Long projectId) {
  TicketCustomerProject p = manualProject(customerId, projectId, leadOrg());
  if (equipment.existsByCustomerProject_Id(projectId)) throw conflict("Equipment is linked to this project; change it first.");
  manualProjects.delete(p);
 }
 /** Year shown for a Projectum project: the year of its planned start (Projectum has no separate year field). */
 public static Integer projectYear(Project p) { return p == null || p.getPlannedStart() == null ? null : p.getPlannedStart().getYear(); }

 /** Choices for the ticket form. A client only gets the customer their account is linked to. */
 public FormOptions formOptions(Long customerId) {
  AppUser me = settings.current(); Long org = me.getOrganisation() == null ? null : me.getOrganisation().getId();
  if (org == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No organisation.");
  List<Ref> choices;
  if (me.hasAnyRole(TicketSettingsService.SUPPORT_ROLES)) {
   // Every customer of the organisation (no active filter, no limit) so historical customers can be chosen too
   choices = customers.findByOrganisation_IdOrderByNameAsc(org).stream().map(c -> new Ref(c.getId(), c.isActive() ? c.getName() : c.getName() + " (inactive)")).toList();
  } else {
   Customer own = customerOfClient(me);
   choices = own == null ? List.of() : List.of(new Ref(own.getId(), own.getName()));
   customerId = own == null ? null : own.getId();
  }
  Long chosen = customerId;
  if (chosen == null || choices.stream().noneMatch(c -> c.id().equals(chosen))) {
   // Without a registered customer (none or a manual one) staff may still link any Projectum project of the organisation
   List<ProjectDto> any = me.hasAnyRole(TicketSettingsService.SUPPORT_ROLES) ? projectsOf(org, null) : List.of();
   return new FormOptions(choices, null, List.of(), List.of(), any);
  }
  return new FormOptions(choices, chosen, sitesOf(org, chosen), equipmentOf(org, chosen), projectsOf(org, chosen));
 }

 // ---------- manual customers and import ----------
 /**
  * A customer typed by hand on a ticket is saved as a customer of the organisation (reused when one with the same name
  * exists), so it is stored once and offered in the customer lists afterwards.
  * TODO(confirm with Anis): manual customers previously stayed a name on the ticket only; confirm they should become customer records.
  */
 public Customer findOrCreateManualCustomer(String name, Long org) {
  String n = trim(name); if (n == null) return null;
  return customers.findFirstByOrganisation_IdAndNameIgnoreCase(org, n).orElseGet(() -> {
   Customer c = new Customer(); c.setOrganisation(settings.current().getOrganisation()); c.setName(n); c.setCustomerCode(uniqueCode(org, n)); c.setActive(true);
   return customers.save(c);
  });
 }

 /**
  * Import of historical customers and their orders (one row per customer, or per customer + order). Customers are matched
  * by code, then by name (case-insensitive); orders by number, then by name + year, within the customer. Only orders of
  * 2023 to 2026 are accepted. dryRun reports what would happen without saving.
  */
 public ImportResult importCustomers(ImportRequest request) {
  Long org = leadOrg(); var organisation = settings.current().getOrganisation();
  int customersCreated = 0, customersExisting = 0, ordersCreated = 0, ordersDuplicate = 0, skipped = 0; List<String> errors = new ArrayList<>();
  Map<String, Customer> created = new HashMap<>(); Set<String> seenOrders = new HashSet<>();
  int line = 1;
  for (ImportRow r : request.rows()) {
   line++;
   String name = trim(r.customerName()), code = trim(r.customerCode());
   if (name == null && code == null) { skipped++; errors.add("Row " + line + ": customer name or code is required."); continue; }
   Customer c = code != null ? customers.findByOrganisation_IdAndCustomerCodeIgnoreCase(org, code).orElse(null) : null;
   if (c == null && name != null) c = customers.findFirstByOrganisation_IdAndNameIgnoreCase(org, name).orElse(null);
   String key = (code != null ? "C:" + code : "N:" + name).toLowerCase(Locale.ROOT);
   if (c == null) c = created.get(key);
   if (c == null && name == null) { skipped++; errors.add("Row " + line + ": unknown customer code " + code + " and no name to create it."); continue; }
   if (c == null) {
    c = new Customer(); c.setOrganisation(organisation); c.setName(name); c.setCustomerCode(code != null ? code : uniqueCode(org, name)); c.setActive(true);
    c.setCountry(trim(r.country())); c.setTown(trim(r.town())); c.setEmail(trim(r.email())); c.setPhone(trim(r.phone()));
    if (!request.dryRun()) c = customers.save(c);
    created.put(key, c); if (name != null) created.put(("N:" + name).toLowerCase(Locale.ROOT), c); customersCreated++;
   } else if (!created.containsValue(c)) customersExisting++;
   String number = trim(r.orderNumber()), orderName = trim(r.orderName());
   if (number == null && orderName == null) continue;
   if (r.year() == null || r.year() < 2023 || r.year() > 2026) { skipped++; errors.add("Row " + line + ": order year must be between 2023 and 2026."); continue; }
   String customerKey = c.getId() == null ? key : String.valueOf(c.getId());
   String orderKey = customerKey + "|" + (number != null ? "#" + number.toLowerCase(Locale.ROOT) : orderName.toLowerCase(Locale.ROOT) + "|" + r.year());
   final Customer owner = c;
   boolean exists = !seenOrders.add(orderKey) || (owner.getId() != null && manualProjects.findByOrganisation_IdAndCustomer_IdOrderByNameAsc(org, owner.getId()).stream().anyMatch(p -> number != null
     ? number.equalsIgnoreCase(Objects.toString(p.getProjectNumber(), "")) : orderName.equalsIgnoreCase(p.getName()) && Objects.equals(p.getProjectYear(), r.year())))
     || (owner.getId() != null && number != null && projectsOf(org, owner.getId()).stream().anyMatch(p -> number.equalsIgnoreCase(Objects.toString(p.code(), ""))));
   if (exists) { ordersDuplicate++; continue; }
   if (!request.dryRun()) {
    TicketCustomerProject p = new TicketCustomerProject(); p.setOrganisation(organisation); p.setCustomer(owner);
    p.setName(orderName != null ? orderName : number); p.setProjectNumber(number); p.setProjectYear(r.year()); manualProjects.save(p);
   }
   ordersCreated++;
  }
  return new ImportResult(request.rows().size(), customersCreated, customersExisting, ordersCreated, ordersDuplicate, skipped, errors.size() > 200 ? errors.subList(0, 200) : errors, request.dryRun());
 }

 /** Customer code derived from the name, unique in the organisation (codes are required on customers). */
 private String uniqueCode(Long org, String name) {
  String base = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "-").replaceAll("^-+|-+$", "");
  if (base.isEmpty()) base = "CUST"; if (base.length() > 50) base = base.substring(0, 50);
  String code = base; int i = 2;
  while (customers.existsByOrganisation_IdAndCustomerCodeIgnoreCase(org, code)) code = base + "-" + i++;
  return code;
 }

 // ---------- used by TicketService ----------
 /** Customer a CLIENT account belongs to, through the customer contact linked to it. */
 public Customer customerOfClient(AppUser client) { return client == null ? null : contacts.findFirstByPortalUser_Id(client.getId()).map(TicketCustomerContact::getCustomer).orElse(null); }
 public Customer requireCustomer(Long id, Long org) { return customer(id, org); }
 public Project requireProject(Long projectId, Customer customer, Long org) {
  Project p = projects.findByIdAndOrganisationId(projectId, org).orElseThrow(() -> bad("Project not found."));
  if (customer != null && p.getCustomerEntity() != null && !p.getCustomerEntity().getId().equals(customer.getId())) throw bad("The project belongs to another customer.");
  return p;
 }
 public TicketCustomerEquipment requireEquipment(Long equipmentId, Customer customer, Long org) {
  TicketCustomerEquipment e = equipment.findByIdAndOrganisation_Id(equipmentId, org).orElseThrow(() -> bad("Equipment not found."));
  if (customer == null || !e.getCustomer().getId().equals(customer.getId())) throw bad("The equipment belongs to another customer.");
  return e;
 }
 public EquipmentDto equipment(TicketCustomerEquipment e) {
  CrmEquipmentType t = e.getEquipmentType(); Project p = e.getProject(); TicketCustomerProject m = e.getCustomerProject();
  String projectName = p != null ? p.getName() : m != null ? m.getName() : null;
  return new EquipmentDto(e.getId(), t == null ? null : t.getId(), t == null ? null : t.getName(), e.getName(), e.getSerialNumber(), e.getNote(), e.getSite() == null ? null : e.getSite().getId(), e.getSite() == null ? null : e.getSite().getName(),
    p == null ? null : p.getId(), m == null ? null : m.getId(), projectName);
 }

 // ---------- helpers ----------
 private CustomerDto customer(Customer c, List<Project> orgProjects) {
  long projectCount = orgProjects.stream().filter(p -> p.getCustomerEntity() != null && p.getCustomerEntity().getId().equals(c.getId())).count() + manualProjects.countByCustomer_Id(c.getId());
  return new CustomerDto(c.getId(), c.getCustomerCode(), c.getName(), c.getCountry(), c.getTown(), c.getAddress(), c.getEmail(), c.getPhone(), c.getContactPerson(), c.isActive(),
    sites.countByCustomer_Id(c.getId()), contacts.countByCustomer_Id(c.getId()), equipment.countByCustomer_Id(c.getId()), projectCount, tickets.countByCustomer_IdAndStatusNotIn(c.getId(), TicketSettingsService.DONE));
 }
 private void apply(Customer c, CustomerRequest r) {
  c.setCustomerCode(r.code().trim()); c.setName(r.name().trim()); c.setCountry(trim(r.country())); c.setTown(trim(r.town())); c.setAddress(trim(r.address()));
  c.setEmail(trim(r.email())); c.setPhone(trim(r.phone())); c.setContactPerson(trim(r.contactPerson())); if (r.active() != null) c.setActive(r.active());
 }
 private void apply(TicketCustomerContact x, ContactRequest r, Long org) {
  x.setFullName(r.fullName().trim()); x.setJobTitle(trim(r.jobTitle())); x.setEmail(trim(r.email())); x.setPhone(trim(r.phone()));
  String lang = trim(r.language()); if (lang != null && !TicketSettingsService.LANGUAGES.contains(lang.toLowerCase())) throw bad("Unsupported language " + lang + "."); x.setLanguage(lang == null ? null : lang.toLowerCase());
  if (r.portalUserId() == null) { x.setPortalUser(null); return; }
  AppUser u = users.findByIdAndOrganisation_Id(r.portalUserId(), org).orElseThrow(() -> bad("Portal account not found."));
  if (!u.hasRole(Role.CLIENT)) throw bad("Only CLIENT accounts can be linked to a contact.");
  contacts.findFirstByPortalUser_Id(u.getId()).filter(o -> !o.getId().equals(x.getId())).ifPresent(o -> { throw conflict(u.getFullName() + " is already linked to contact " + o.getFullName() + " of " + o.getCustomer().getName() + "."); });
  x.setPortalUser(u);
 }
 private void apply(TicketCustomerEquipment e, EquipmentRequest r, Long org, Long customerId) {
  e.setName(r.name().trim()); e.setSerialNumber(trim(r.serialNumber())); e.setNote(trim(r.note()));
  e.setEquipmentType(r.typeId() == null ? null : equipmentTypes.findByIdAndOrganisation_Id(r.typeId(), org).orElseThrow(() -> bad("Equipment type not found.")));
  e.setSite(r.siteId() == null ? null : site(customerId, r.siteId(), org));
  // A project of the customer's Projects tab: Projectum (projectId) or manually added (customerProjectId)
  if (r.projectId() != null && r.customerProjectId() != null) throw bad("Choose one project for the equipment.");
  e.setProject(r.projectId() == null ? null : requireProject(r.projectId(), e.getCustomer(), org));
  e.setCustomerProject(r.customerProjectId() == null ? null : manualProject(customerId, r.customerProjectId(), org));
 }
 private void apply(TicketCustomerProject p, ManualProjectRequest r) { p.setName(r.name().trim()); p.setProjectNumber(trim(r.projectNumber())); p.setProjectYear(r.year()); }
 private List<SiteDto> sitesOf(Long org, Long customerId) {
  List<TicketCustomerEquipment> items = equipment.findByOrganisation_IdAndCustomer_IdOrderByNameAsc(org, customerId);
  return sites.findByOrganisation_IdAndCustomer_IdOrderByNameAsc(org, customerId).stream()
    .map(s -> new SiteDto(s.getId(), s.getName(), s.getAddress(), items.stream().filter(e -> e.getSite() != null && e.getSite().getId().equals(s.getId())).count())).toList();
 }
 private List<EquipmentDto> equipmentOf(Long org, Long customerId) { return equipment.findByOrganisation_IdAndCustomer_IdOrderByNameAsc(org, customerId).stream().map(this::equipment).toList(); }
 /** Projectum projects of a customer, or of the whole organisation when customerId is null. */
 private List<ProjectDto> projectsOf(Long org, Long customerId) {
  return projects.findAllByOrganisationId(org).stream().filter(p -> customerId == null || (p.getCustomerEntity() != null && p.getCustomerEntity().getId().equals(customerId)))
    .sorted(Comparator.comparing(p -> Objects.toString(p.getName(), ""))).map(p -> new ProjectDto(p.getId(), p.getCode(), p.getName(), p.getProjectPhase(), projectYear(p), false)).toList();
 }
 private static ProjectDto project(TicketCustomerProject p) { return new ProjectDto(p.getId(), p.getProjectNumber(), p.getName(), null, p.getProjectYear(), true); }
 private SiteDto site(TicketCustomerSite s) { return new SiteDto(s.getId(), s.getName(), s.getAddress(), equipment.countBySite_Id(s.getId())); }
 private ContactDto contact(TicketCustomerContact x) { return new ContactDto(x.getId(), x.getFullName(), x.getJobTitle(), x.getEmail(), x.getPhone(), x.getLanguage(), user(x.getPortalUser())); }
 private UserSummary user(AppUser u) { return u == null ? null : new UserSummary(u.getId(), u.getFullName(), u.getEmail(), u.getPrimaryRole().name()); }
 private Customer customer(Long id, Long org) { return customers.findByIdAndOrganisation_Id(id, org).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found.")); }
 private TicketCustomerSite site(Long customerId, Long id, Long org) { return sites.findByIdAndOrganisation_Id(id, org).filter(s -> s.getCustomer().getId().equals(customerId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found.")); }
 private TicketCustomerContact contact(Long customerId, Long id, Long org) { return contacts.findByIdAndOrganisation_Id(id, org).filter(s -> s.getCustomer().getId().equals(customerId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found.")); }
 private TicketCustomerProject manualProject(Long customerId, Long id, Long org) { return manualProjects.findByIdAndOrganisation_Id(id, org).filter(p -> p.getCustomer().getId().equals(customerId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found.")); }
 private TicketCustomerEquipment equipment(Long customerId, Long id, Long org) { return equipment.findByIdAndOrganisation_Id(id, org).filter(s -> s.getCustomer().getId().equals(customerId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found.")); }
 private Long staffOrg() { return settings.requireStaff().getOrganisation().getId(); }
 private Long leadOrg() { return settings.requireLead().getOrganisation().getId(); }
 private static String trim(String v) { return v == null || v.trim().isEmpty() ? null : v.trim(); }
 private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
 private static ResponseStatusException conflict(String m) { return new ResponseStatusException(HttpStatus.CONFLICT, m); }
}
