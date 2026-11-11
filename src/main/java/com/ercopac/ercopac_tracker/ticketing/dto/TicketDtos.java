package com.ercopac.ercopac_tracker.ticketing.dto;
import com.ercopac.ercopac_tracker.ticketing.domain.*; import jakarta.validation.constraints.*; import java.time.Instant; import java.util.List;
public final class TicketDtos { private TicketDtos(){}
 // customerId / projectId link Projectum records; when they are null, customerName and projectName/Number/Year are a manual entry
 // category: code of a system (TicketCategory) or custom category of Ticket settings
 public record CreateTicketRequest(@NotBlank @Size(max=250) String subject,@NotBlank @Size(max=8000) String description,@NotBlank @Size(max=32) String category,@NotNull TicketPriority priority,TicketOrigin origin,@Size(max=160) String site,Long organisationId,Long clientUserId,@Size(max=8000) String initialMessage,Long customerId,Long projectId,Long equipmentId,Long assigneeId,
   @Size(max=180) String customerName,@Size(max=250) String projectName,@Size(max=60) String projectNumber,@Min(1900) @Max(2100) Integer projectYear,
   @Size(max=150) String contactName,@Email @Size(max=180) String contactEmail,@Size(max=60) @Pattern(regexp=PHONE) String contactPhone){
  public CreateTicketRequest(String subject,String description,String category,TicketPriority priority,TicketOrigin origin,String site,Long organisationId,Long clientUserId,String initialMessage,Long customerId,Long projectId,Long equipmentId,Long assigneeId){this(subject,description,category,priority,origin,site,organisationId,clientUserId,initialMessage,customerId,projectId,equipmentId,assigneeId,null,null,null,null,null,null,null);}
 }
 /** Phone of a contact person: digits, spaces and + ( ) . - / only (empty allowed). */
 public static final String PHONE="^$|^[0-9+()./ \\-]{3,60}$";
 public record UpdateTicketRequest(@Size(max=250) String subject,@Size(max=8000) String description,@Size(max=32) String category,TicketOrigin origin,@Size(max=160) String site,Long version){}
 // Contact person at the customer for one ticket; every value is replaced (null / blank clears it)
 public record ContactPersonRequest(@Size(max=150) String contactName,@Email @Size(max=180) String contactEmail,@Size(max=60) @Pattern(regexp=PHONE) String contactPhone,@NotNull Long version){}
 // Customer, site, project and equipment of a ticket; every value is replaced (null clears it).
 // A null customerId / projectId with a name is a manual entry (historical or external record).
 public record TicketContextRequest(Long customerId,@Size(max=160) String site,Long projectId,Long equipmentId,@NotNull Long version,
   @Size(max=180) String customerName,@Size(max=250) String projectName,@Size(max=60) String projectNumber,@Min(1900) @Max(2100) Integer projectYear){
  public TicketContextRequest(Long customerId,String site,Long projectId,Long equipmentId,Long version){this(customerId,site,projectId,equipmentId,version,null,null,null,null);}
 }
 public record ProblemStatementRequest(@Size(max=4000) String problem,@Size(max=4000) String cause,@Size(max=4000) String solution,@NotNull Long version){}
 public record StatusRequest(TicketStatus status,@NotNull Long version,Long customStatusId){ public StatusRequest(TicketStatus status,Long version){this(status,version,null);} } public record PriorityRequest(@NotNull TicketPriority priority,@NotNull Long version){} public record AssignmentRequest(Long assigneeId,@NotNull Long version){} public record MessageRequest(@NotBlank @Size(max=8000) String message,boolean internalNote){}
 public record Ref(Long id,String name){}
 public record UserSummary(Long id,String fullName,String email,String role){} public record TicketSummary(Long id,String ticketNumber,String subject,Long organisationId,String organisationName,TicketStatus status,TicketPriority priority,String category,String site,UserSummary client,UserSummary assignee,Instant createdAt,Instant updatedAt,Instant lastMessageAt,long unreadCount,Ref customer,Ref project,UserSummary l2Technician,boolean overdue,String projectNumber,Integer projectYear,CustomStatusRef customStatus){}
 public record CustomStatusRef(Long id,String label,String color){}
 public record ContactPerson(String name,String email,String phone){}
 public record TicketDetails(TicketSummary ticket,String description,TicketOrigin origin,int escalationLevel,Instant resolvedAt,Instant closedAt,Long version,List<TicketMessageDto> messages,List<TicketActivityDto> activities,List<TicketAttachmentDto> attachments,boolean canManage,boolean canClose,EquipmentDto equipment,String problem,String cause,String solution,String clientLanguage,ContactPerson contact,boolean canDelete){}
 public record TicketMessageDto(Long id,String message,String messageType,boolean internalNote,UserSummary sender,Instant createdAt,Instant editedAt){} public record TicketActivityDto(Long id,TicketActivityType activityType,String previousValue,String newValue,String description,UserSummary actor,Instant createdAt,String field){} public record TicketAttachmentDto(Long id,String originalFileName,String contentType,long fileSize,UserSummary uploadedBy,Instant uploadedAt){}
 public record TicketStatistics(long open,long inProgress,long escalated,long resolved,long closed,long createdToday,long overdue,double averageResolutionHours,List<Breakdown> byPriority,List<Breakdown> byCategory){}
 // Client portal dashboard: statistics of the current client's own tickets only (filtered in the backend)
 public record ClientDashboard(long total,long open,long inProgress,long resolved,long closed,long overdue,double averageResolutionHours,List<Breakdown> byStatus,List<Breakdown> byPriority,List<Breakdown> byCategory,List<Breakdown> byMonth,List<TicketSummary> recent){} public record Breakdown(String key,long count){}
 // Live chat (one-to-one, not linked to a ticket)
 public record ChatContact(Long id,String fullName,String email,String kind,boolean staff){} public record ChatConversation(ChatContact contact,String lastMessage,Instant lastMessageAt,boolean lastFromMe,long unread){} public record ChatMessageDto(Long id,Long senderId,Long recipientId,String message,Instant createdAt,Instant readAt){} public record ChatMessageRequest(@NotBlank @Size(max=4000) String message){}

 // Settings: status wording, priority SLA targets, support team profiles and the current user's language
 public record StatusSettingDto(@NotNull TicketStatus status,@NotBlank @Size(max=60) String label,@Size(max=300) String description,@NotBlank @Size(max=20) String color){}
 public record PrioritySettingDto(@NotNull TicketPriority priority,@NotBlank @Size(max=60) String label,@Min(1) @Max(100000) int firstResponseMinutes,@Min(1) @Max(10000) int resolutionHours){}
 public record TicketSettingsDto(List<StatusSettingDto> statuses,List<PrioritySettingDto> priorities,List<CategoryDto> categories,List<CustomStatusDto> customStatuses){
  public TicketSettingsDto(List<StatusSettingDto> statuses,List<PrioritySettingDto> priorities){this(statuses,priorities,List.of(),List.of());}
 }
 // Categories (system ones cannot be deleted) and custom statuses mapped onto a lifecycle status
 public record CategoryDto(Long id,String code,String label,boolean system,boolean active,long ticketCount){}
 public record CategoryRequest(@Size(max=32) String code,@NotBlank @Size(max=80) String label,Boolean active){}
 public record CustomStatusDto(Long id,String code,String label,String description,String color,TicketStatus baseStatus,long ticketCount){}
 public record CustomStatusRequest(@Size(max=40) String code,@NotBlank @Size(max=60) String label,@Size(max=300) String description,@NotBlank @Size(max=20) String color,@NotNull TicketStatus baseStatus){}
 // Support account created from Ticketing settings: kind H24 (agent), H24_LEAD (lead) or TECHNICIAN (employee flagged as L2 technician)
 public record AccountRequest(@NotBlank @Size(max=150) String fullName,@NotBlank @Email @Size(max=180) String email,@NotBlank @Size(min=8,max=128) String password,@NotBlank String kind,Long departmentId,Long resourceTypeId,@Size(max=120) String specialization){}
 public record AgentDto(Long id,String fullName,String email,List<String> roles,boolean lead,boolean l2Technician,String specialization,String preferredLanguage,long openTickets,long l2OpenTickets,boolean active,Long resourceTypeId,String resourceType,boolean h24,boolean ticketingAccess){}
 // Projectum resource types and the employees of one type (cascading L2 choice)
 public record ResourceTypeOption(Long id,String code,String label){}
 public record EmployeeOption(Long id,String fullName,String email,Long resourceTypeId,String resourceType,boolean l2Technician,String specialization){}
 public record AgentProfileRequest(boolean l2Technician,@Size(max=120) String specialization,@Size(max=8) String preferredLanguage){}
 public record LanguageRequest(@Size(max=8) String language){}
 public record TranslateRequest(@NotBlank @Size(max=2000) String text,@NotBlank @Size(max=8) String to){} public record TranslateResponse(String text){}
 public record MySettingsDto(Long userId,String fullName,String preferredLanguage,boolean lead,boolean staff,Long customerId){}

 // Customers: the organisation customer companies with their sites, contacts, equipment and projects
 public record CustomerDto(Long id,String code,String name,String country,String town,String address,String email,String phone,String contactPerson,boolean active,long sites,long contacts,long equipment,long projects,long openTickets){}
 public record CustomerRequest(@NotBlank @Size(max=60) String code,@NotBlank @Size(max=180) String name,@Size(max=80) String country,@Size(max=120) String town,@Size(max=300) String address,@Email @Size(max=180) String email,@Size(max=60) String phone,@Size(max=150) String contactPerson,Boolean active){}
 public record SiteDto(Long id,String name,String address,long equipment){} public record SiteRequest(@NotBlank @Size(max=160) String name,@Size(max=300) String address){}
 public record ContactDto(Long id,String fullName,String jobTitle,String email,String phone,String language,UserSummary portalUser){}
 public record ContactRequest(@NotBlank @Size(max=150) String fullName,@Size(max=120) String jobTitle,@Email @Size(max=180) String email,@Size(max=60) String phone,@Size(max=8) String language,Long portalUserId){}
 // Equipment project: projectId (Projectum) or customerProjectId (manual project of the Projects tab), at most one
 public record EquipmentDto(Long id,Long typeId,String typeName,String name,String serialNumber,String note,Long siteId,String siteName,Long projectId,Long customerProjectId,String projectName){}
 public record EquipmentRequest(Long typeId,@NotBlank @Size(max=160) String name,@Size(max=100) String serialNumber,@Size(max=500) String note,Long siteId,Long projectId,Long customerProjectId){
  public EquipmentRequest(Long typeId,String name,String serialNumber,String note,Long siteId){this(typeId,name,serialNumber,note,siteId,null,null);}
 }
 // Projects tab: Projectum projects of the customer (manual = false, id = Projectum project id) and manually added ones (manual = true)
 public record ProjectDto(Long id,String code,String name,String phase,Integer year,boolean manual){
  public ProjectDto(Long id,String code,String name,String phase){this(id,code,name,phase,null,false);}
 }
 public record ManualProjectRequest(@NotBlank @Size(max=250) String name,@Size(max=60) String projectNumber,@Min(1900) @Max(2100) Integer year){}
 // Choices for the ticket form: customers the user may file under and, for the chosen customer, its sites, equipment and projects
 public record FormOptions(List<Ref> customers,Long customerId,List<SiteDto> sites,List<EquipmentDto> equipment,List<ProjectDto> projects){}
 // Import of historical customers and their orders (projects / "commesse"); one row per customer or customer + order
 public record ImportRow(@Size(max=60) String customerCode,@Size(max=180) String customerName,@Size(max=80) String country,@Size(max=120) String town,@Size(max=180) String email,@Size(max=60) String phone,@Size(max=60) String orderNumber,@Size(max=250) String orderName,Integer year){}
 public record ImportRequest(@NotNull @Size(max=10000) List<@jakarta.validation.Valid ImportRow> rows,boolean dryRun){}
 public record ImportResult(int rows,int customersCreated,int customersExisting,int ordersCreated,int ordersDuplicate,int skipped,List<String> errors,boolean dryRun){}

 // Performance dashboard
 public record AnalyticsTotals(long total,long open,long resolved,long escalated,long overdue,Double avgFirstResponseHours,Double avgResolutionHours,Double slaCompliance){}
 public record CustomerStat(Long id,String name,long total,long open,Double slaCompliance){}
 public record AgentStat(Long id,String name,String specialization,boolean l2Technician,long assigned,long open,long resolved,long escalated,Double avgFirstResponseHours,Double slaCompliance){}
 // Tickets matching an equipment filter (listed under the charts when one is set)
 public record EquipmentTicket(Long id,String ticketNumber,String subject,String customer,String site,String equipment,String serialNumber,TicketStatus status){}
 public record TicketAnalytics(Instant from,Instant to,String bucket,AnalyticsTotals totals,List<Breakdown> byStatus,List<Breakdown> byPriority,List<CustomerStat> byCustomer,List<Breakdown> timeline,List<AgentStat> agents,List<EquipmentTicket> equipmentTickets){}
}
