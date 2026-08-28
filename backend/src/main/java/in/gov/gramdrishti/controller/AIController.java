package in.gov.gramdrishti.controller;

import in.gov.gramdrishti.dto.AIDto;
import in.gov.gramdrishti.dto.ApiResponse;
import in.gov.gramdrishti.dto.ProjectDto;
import in.gov.gramdrishti.security.UserPrincipal;
import in.gov.gramdrishti.service.AIServiceClient;
import in.gov.gramdrishti.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI & Closed-Loop Intelligence", description = "Gap Scoring, Computer Vision Detection, and Closed-Loop Impact Evaluation")
public class AIController {

    private final AIServiceClient aiServiceClient;
    private final ProjectService projectService;

    @PostMapping("/gap-score")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'ANALYST')")
    @Operation(summary = "Calculate multi-dimensional rural infrastructure gap score (0-100)")
    public ResponseEntity<ApiResponse<AIDto.AIGapScoreResponse>> calculateGapScore(
            @RequestBody AIDto.AIGapScoreRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AIDto.AIGapScoreResponse response = aiServiceClient.computeGapScore(request, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/analyze")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'ANALYST')")
    @Operation(summary = "Execute comprehensive multi-modal village intelligence analysis")
    public ResponseEntity<ApiResponse<AIDto.AIGapScoreResponse>> analyzeVillage(
            @RequestBody AIDto.AIGapScoreRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AIDto.AIGapScoreResponse response = aiServiceClient.computeGapScore(request, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("Village analysis completed", response));
    }

    @PostMapping(value = "/infrastructure-detection", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'ANALYST')")
    @Operation(summary = "Run Computer Vision / YOLO detection on satellite, drone or ground photos")
    public ResponseEntity<ApiResponse<AIDto.AIDetectionResponse>> detectInfrastructure(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "image_url", required = false) String imageUrl,
            @RequestParam(value = "category_hint", required = false, defaultValue = "ALL") String categoryHint,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AIDto.AIDetectionResponse response = aiServiceClient.detectInfrastructure(
                file, imageUrl, categoryHint, principal != null ? principal.getEmail() : "SYSTEM"
        );
        return ResponseEntity.ok(ApiResponse.ok("Computer vision detection processed", response));
    }

    @PostMapping("/re-evaluate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'ANALYST')")
    @Operation(summary = "Closed-loop impact measurement: re-evaluate gap score post-intervention")
    public ResponseEntity<ApiResponse<AIDto.AIReEvaluationResponse>> reEvaluateProject(
            @RequestBody AIDto.AIReEvaluationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AIDto.AIReEvaluationResponse response = aiServiceClient.reEvaluateProject(
                request, principal != null ? principal.getEmail() : "SYSTEM"
        );
        return ResponseEntity.ok(ApiResponse.ok("Impact re-evaluation verified successfully", response));
    }

    @GetMapping("/results/{projectId}")
    @Operation(summary = "Get historical and current AI intelligence metrics for a project")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProjectAIResults(@PathVariable Long projectId) {
        ProjectDto.ProjectResponseDto project = projectService.getProjectById(projectId);

        Map<String, Object> results = new HashMap<>();
        results.put("projectId", project.getId());
        results.put("projectCode", project.getProjectCode());
        results.put("projectName", project.getProjectName());
        results.put("baselineGapScore", project.getBaselineGapScore());
        results.put("targetGapScore", project.getTargetGapScore());
        results.put("currentGapScore", project.getCurrentGapScore());
        results.put("gapReductionPct", project.getGapReductionPct());
        results.put("impactSummary", project.getImpactSummary());
        results.put("status", project.getStatus());

        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}
