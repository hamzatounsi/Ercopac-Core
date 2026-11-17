package com.ercopac.ercopac_tracker.projectum_ai.web;

import com.ercopac.ercopac_tracker.projectum_ai.dto.AiProjectRequest;
import com.ercopac.ercopac_tracker.projectum_ai.dto.AiProjectResponse;
import com.ercopac.ercopac_tracker.projectum_ai.service.OllamaAiService;
import com.ercopac.ercopac_tracker.projectum_ai.service.ProjectAiContextService;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ProjectAiController {

    private static final String PROJECTS_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).PROJECTS)";

    private final OllamaAiService ollamaAiService;
    private final ProjectAiContextService contextService;
    private final SecurityUtils securityUtils;
    private final ObjectMapper mapper;

    @PostMapping("/test")
    @PreAuthorize(PROJECTS_READ)
    public AiProjectResponse test(@RequestBody AiProjectRequest request) {
        String answer = ollamaAiService.ask(request.question());
        return new AiProjectResponse(answer);
    }

    @PostMapping("/project-assistant/ask")
    @PreAuthorize(PROJECTS_READ)
    public AiProjectResponse askProjectAssistant(@RequestBody AiProjectRequest request) {
        return new AiProjectResponse(ollamaAiService.ask(promptFor(request)));
    }

    @PostMapping(value = "/project-assistant/stream", produces = "application/x-ndjson")
    @PreAuthorize(PROJECTS_READ)
    public ResponseEntity<StreamingResponseBody> streamProjectAssistant(@RequestBody AiProjectRequest request) {
        String prompt = promptFor(request);
        StreamingResponseBody body = output -> {
            try {
                ollamaAiService.stream(prompt, token -> writeEvent(output, "token", token));
                writeEvent(output, "done", "");
            } catch (ResponseStatusException e) {
                writeEvent(output, "error", e.getReason() == null ? "AI unavailable" : e.getReason());
            }
        };
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/x-ndjson")).body(body);
    }

    private String promptFor(AiProjectRequest request) {
        if (request == null || request.question() == null || request.question().isBlank()
                || request.question().length() > 500) {
            throw new IllegalArgumentException("Question must be between 1 and 500 characters");
        }
        String question = request.question().trim();
        String context = securityUtils.isPlatformUser()
                ? contextService.buildPlatformProjectContext(request.projectId(), question)
                : contextService.buildProjectContext(
                        request.projectId(),
                        securityUtils.getCurrentOrganisationId(), question
                );
        String format = question.toLowerCase(java.util.Locale.ROOT).contains("report")
                ? "Exactly four plain lines, no title or markdown, at most 30 words each: Status:, Progress:, Concern:, Next:. "
                : "Answer in 3-5 clear sentences. Use short '- ' bullet lines when listing several items, no other markdown. ";
        return format + "Use exact values and dates from the facts only. Samples are incomplete; never infer all rows. "
                + "If missing, say so. Give one practical next action when useful.\n" + context + "\nQuestion: " + question;
    }

    private void writeEvent(java.io.OutputStream output, String key, String value) {
        try {
            output.write((mapper.writeValueAsString(Map.of(key, value)) + "\n").getBytes(StandardCharsets.UTF_8));
            output.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
