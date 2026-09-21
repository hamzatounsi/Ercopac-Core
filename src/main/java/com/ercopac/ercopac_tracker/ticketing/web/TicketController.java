package com.ercopac.ercopac_tracker.ticketing.web;

import com.ercopac.ercopac_tracker.ticketing.domain.*;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.service.*;
import com.ercopac.ercopac_tracker.ticketing.websocket.TicketWebSocketHandler;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize; // 👈 IMPORT OBLIGATOIRE
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService service;
    private final TicketAttachmentService attachments;
    private final TicketWebSocketHandler chat;

    public TicketController(TicketService service, TicketAttachmentService attachments, TicketWebSocketHandler chat) {
        this.service = service;
        this.attachments = attachments;
        this.chat = chat;
    }

    // --- CONSTANTES DE SÉCURITÉ (incluant H24 et H24_LEAD) ---
    
    // 1. Accès standard : Lecture, création, messages
    private static final String STANDARD_ACCESS = 
        "hasAnyRole('H24', 'H24_LEAD', 'PLATFORM_OWNER', 'ORG_ADMIN', 'PROJECT_MANAGER', " +
        "'PROJECT_MANAGER_LEAD', 'MANAGER', 'DEPARTMENT_MANAGER', 'EMPLOYEE', 'CLIENT')";
    
    // 2. Accès avancé : Modification, clôture, réouverture, assignation
    private static final String ADVANCED_ACCESS = 
        "hasAnyRole('H24', 'H24_LEAD', 'PLATFORM_OWNER', 'ORG_ADMIN', 'PROJECT_MANAGER', " +
        "'PROJECT_MANAGER_LEAD', 'MANAGER', 'DEPARTMENT_MANAGER')";

    // 3. Accès critique : Escalade, suppression (Réservé aux Leads et Admins)
    private static final String CRITICAL_ACCESS = 
        "hasAnyRole('H24_LEAD', 'PLATFORM_OWNER', 'ORG_ADMIN', 'PROJECT_MANAGER_LEAD', " +
        "'MANAGER', 'DEPARTMENT_MANAGER')";

    @GetMapping
    @PreAuthorize(STANDARD_ACCESS)
    public Page<TicketSummary> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) Long organisationId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort) {
        
        String[] s = sort.split(",");
        return service.list(
                search, status, priority, category, organisationId, assigneeId, creatorId, from, to,
                PageRequest.of(
                        Math.max(0, page),
                        Math.min(Math.max(1, size), 100),
                        Sort.by(s.length > 1 && "asc".equalsIgnoreCase(s[1]) ? Sort.Direction.ASC : Sort.Direction.DESC, s[0])
                )
        );
    }

    @GetMapping("/statistics")
    @PreAuthorize(STANDARD_ACCESS)
    public TicketStatistics stats() {
        return service.stats();
    }

    @GetMapping("/{id}")
    @PreAuthorize(STANDARD_ACCESS)
    public TicketDetails get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize(STANDARD_ACCESS)
    public ResponseEntity<TicketDetails> create(@Valid @RequestBody CreateTicketRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    @PatchMapping("/{id}")
    @PreAuthorize(ADVANCED_ACCESS)
    public TicketDetails update(@PathVariable Long id, @Valid @RequestBody UpdateTicketRequest r) {
        return service.update(id, r);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize(ADVANCED_ACCESS)
    public TicketDetails status(@PathVariable Long id, @Valid @RequestBody StatusRequest r) {
        return service.changeStatus(id, r);
    }

    @PatchMapping("/{id}/priority")
    @PreAuthorize(ADVANCED_ACCESS)
    public TicketDetails priority(@PathVariable Long id, @Valid @RequestBody PriorityRequest r) {
        return service.changePriority(id, r);
    }

    @PatchMapping("/{id}/assignment")
    @PreAuthorize(ADVANCED_ACCESS)
    public TicketDetails assign(@PathVariable Long id, @Valid @RequestBody AssignmentRequest r) {
        return service.assign(id, r);
    }

    @PostMapping("/{id}/escalate")
    @PreAuthorize(CRITICAL_ACCESS)
    public TicketDetails escalate(@PathVariable Long id, @RequestParam Long version) {
        return service.escalate(id, version);
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize(ADVANCED_ACCESS)
    public TicketDetails resolve(@PathVariable Long id, @RequestParam Long version) {
        return service.resolve(id, version);
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize(ADVANCED_ACCESS)
    public TicketDetails reopen(@PathVariable Long id, @RequestParam Long version) {
        return service.reopen(id, version);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize(ADVANCED_ACCESS)
    public TicketDetails close(@PathVariable Long id, @RequestParam Long version) {
        return service.close(id, version);
    }

    @GetMapping("/{id}/messages")
    @PreAuthorize(STANDARD_ACCESS)
    public List<TicketMessageDto> messages(@PathVariable Long id) {
        return service.messages(id);
    }

    @PostMapping("/{id}/messages")
    @PreAuthorize(STANDARD_ACCESS)
    public TicketMessageDto message(@PathVariable Long id, @Valid @RequestBody MessageRequest r) {
        TicketMessageDto saved = service.addMessage(id, r);
        chat.broadcastMessage(id, saved);
        return saved;
    }

    @GetMapping("/{id}/activities")
    @PreAuthorize(STANDARD_ACCESS)
    public List<TicketActivityDto> activity(@PathVariable Long id) {
        return service.activity(id);
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(STANDARD_ACCESS)
    public TicketAttachmentDto upload(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return attachments.upload(id, file);
    }

    @GetMapping("/{id}/attachments/{attachmentId}")
    @PreAuthorize(STANDARD_ACCESS)
    public ResponseEntity<Resource> download(@PathVariable Long id, @PathVariable Long attachmentId) {
        Resource r = attachments.download(id, attachmentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + r.getFilename() + "\"")
                .body(r);
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    @PreAuthorize(CRITICAL_ACCESS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @PathVariable Long attachmentId) {
        attachments.delete(id, attachmentId);
    }
}