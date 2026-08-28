package in.gov.gramdrishti.controller;

import in.gov.gramdrishti.dto.ApiResponse;
import in.gov.gramdrishti.dto.RequirementDto;
import in.gov.gramdrishti.entity.PriorityLevel;
import in.gov.gramdrishti.entity.RequirementCategory;
import in.gov.gramdrishti.entity.RequirementStatus;
import in.gov.gramdrishti.security.UserPrincipal;
import in.gov.gramdrishti.service.RequirementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
@Tag(name = "Requirements & Gap Interventions", description = "Triage, approve and track infrastructure deficits")
public class RequirementController {

    private final RequirementService requirementService;

    @GetMapping
    @Operation(summary = "List and search requirements with multi-dimensional filters")
    public ResponseEntity<ApiResponse<Page<RequirementDto.RequirementResponseDto>>> getRequirements(
            @RequestParam(required = false) RequirementStatus status,
            @RequestParam(required = false) RequirementCategory category,
            @RequestParam(required = false) PriorityLevel priority,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long villageId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<RequirementDto.RequirementResponseDto> reqs = requirementService.getRequirements(status, category, priority, projectId, villageId, query, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.ok(reqs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get requirement details by ID")
    public ResponseEntity<ApiResponse<RequirementDto.RequirementResponseDto>> getRequirementById(@PathVariable Long id) {
        RequirementDto.RequirementResponseDto req = requirementService.getRequirementById(id);
        return ResponseEntity.ok(ApiResponse.ok(req));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'ANALYST')")
    @Operation(summary = "Create an infrastructure requirement / deficit entry")
    public ResponseEntity<ApiResponse<RequirementDto.RequirementResponseDto>> createRequirement(
            @Valid @RequestBody RequirementDto.RequirementCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RequirementDto.RequirementResponseDto created = requirementService.createRequirement(request, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Requirement created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'ANALYST')")
    @Operation(summary = "Update requirement details")
    public ResponseEntity<ApiResponse<RequirementDto.RequirementResponseDto>> updateRequirement(
            @PathVariable Long id,
            @RequestBody RequirementDto.RequirementUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RequirementDto.RequirementResponseDto updated = requirementService.updateRequirement(id, request, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("Requirement updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Approve, progress, or resolve requirement (Super Admin, Admin, PM)")
    public ResponseEntity<ApiResponse<RequirementDto.RequirementResponseDto>> updateRequirementStatus(
            @PathVariable Long id,
            @Valid @RequestBody RequirementDto.RequirementStatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RequirementDto.RequirementResponseDto updated = requirementService.updateRequirementStatus(id, request, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("Requirement status updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Delete requirement")
    public ResponseEntity<ApiResponse<Void>> deleteRequirement(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        requirementService.deleteRequirement(id, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("Requirement deleted successfully", null));
    }
}
