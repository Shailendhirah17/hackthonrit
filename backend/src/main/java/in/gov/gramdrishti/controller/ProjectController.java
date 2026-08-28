package in.gov.gramdrishti.controller;

import in.gov.gramdrishti.dto.ApiResponse;
import in.gov.gramdrishti.dto.ProjectDto;
import in.gov.gramdrishti.entity.PriorityLevel;
import in.gov.gramdrishti.entity.ProjectStatus;
import in.gov.gramdrishti.security.UserPrincipal;
import in.gov.gramdrishti.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project Management", description = "Lifecycle, Milestone Tracking, and Closed-Loop Progress")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "List and search rural infrastructure projects with filtering")
    public ResponseEntity<ApiResponse<Page<ProjectDto.ProjectResponseDto>>> getProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) PriorityLevel priority,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<ProjectDto.ProjectResponseDto> projects = projectService.getProjects(status, priority, state, district, query, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.ok(projects));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details by ID")
    public ResponseEntity<ApiResponse<ProjectDto.ProjectResponseDto>> getProjectById(@PathVariable Long id) {
        ProjectDto.ProjectResponseDto project = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.ok(project));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Create a new project from identified gap (Super Admin, Admin, PM)")
    public ResponseEntity<ApiResponse<ProjectDto.ProjectResponseDto>> createProject(
            @Valid @RequestBody ProjectDto.ProjectCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProjectDto.ProjectResponseDto created = projectService.createProject(request, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Project created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Update project details, status or milestones (Super Admin, Admin, PM)")
    public ResponseEntity<ApiResponse<ProjectDto.ProjectResponseDto>> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectDto.ProjectUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProjectDto.ProjectResponseDto updated = projectService.updateProject(id, request, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("Project updated successfully", updated));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Assign a user (e.g. Field Officer) to a project")
    public ResponseEntity<ApiResponse<ProjectDto.ProjectAssignmentDto>> assignUser(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDto.AssignUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProjectDto.ProjectAssignmentDto assignment = projectService.assignUserToProject(id, request, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("User assigned to project", assignment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Soft delete a project (Super Admin, Admin, PM)")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        projectService.deleteProject(id, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("Project deleted successfully", null));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Restore a soft-deleted project (Super Admin only)")
    public ResponseEntity<ApiResponse<Void>> restoreProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        projectService.restoreProject(id, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("Project restored successfully", null));
    }
}
