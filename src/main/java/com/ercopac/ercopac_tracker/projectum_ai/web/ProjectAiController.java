package com.ercopac.ercopac_tracker.projectum_ai.web;

import com.ercopac.ercopac_tracker.projectum_ai.dto.AiProjectRequest;
import com.ercopac.ercopac_tracker.projectum_ai.dto.AiProjectResponse;
import com.ercopac.ercopac_tracker.projectum_ai.service.OllamaAiService;
import com.ercopac.ercopac_tracker.projectum_ai.service.ProjectAiContextService;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ProjectAiController {

    private static final String PROJECTS_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).PROJECTS)";

    private final OllamaAiService ollamaAiService;
    private final ProjectAiContextService contextService;
    private final SecurityUtils securityUtils;

    @PostMapping("/test")
    @PreAuthorize(PROJECTS_READ)
    public AiProjectResponse test(@RequestBody AiProjectRequest request) {
        String answer = ollamaAiService.ask(request.question());
        return new AiProjectResponse(answer);
    }

    @PostMapping("/project-assistant/ask")
    @PreAuthorize(PROJECTS_READ)
    public AiProjectResponse askProjectAssistant(@RequestBody AiProjectRequest request) {
        String context = securityUtils.isPlatformUser()
                ? contextService.buildPlatformProjectContext(request.projectId())
                : contextService.buildProjectContext(
                        request.projectId(),
                        securityUtils.getCurrentOrganisationId()
                );

        String prompt = """
                You are Projectum AI Assistant.
                You help project managers analyze project execution, risks, finance, schedule and actions.

                Rules:
                - Use only the provided Projectum context.
                - Do not invent missing data.
                - If data is missing, say clearly that it is not available.
                - Give practical and professional recommendations.
                - Keep the answer structured and concise.

                Context:
                %s

                User question:
                %s
                """.formatted(context, request.question());

        String answer = ollamaAiService.ask(prompt);

        return new AiProjectResponse(answer);
    }
}
