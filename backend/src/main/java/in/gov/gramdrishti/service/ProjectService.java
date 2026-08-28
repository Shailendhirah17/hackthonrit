package in.gov.gramdrishti.service;

import in.gov.gramdrishti.dto.ProjectDto;
import in.gov.gramdrishti.entity.*;
import in.gov.gramdrishti.exception.ResourceNotFoundException;
import in.gov.gramdrishti.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final VillageRepository villageRepository;
    private final UserRepository userRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final RequirementRepository requirementRepository;
    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<ProjectDto.ProjectResponseDto> getProjects(ProjectStatus status, PriorityLevel priority, String state, String district, String query, Pageable pageable) {
        return projectRepository.filterProjects(status, priority, state, district, query, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public ProjectDto.ProjectResponseDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return mapToDto(project);
    }

    @Transactional
    public ProjectDto.ProjectResponseDto createProject(ProjectDto.ProjectCreateRequest request, String performedByEmail) {
        Village village = null;
        if (request.getVillageId() != null) {
            village = villageRepository.findById(request.getVillageId()).orElse(null);
        }

        String projectCode = request.getProjectCode();
        if (projectCode == null || projectCode.trim().isEmpty()) {
            String stateCode = request.getState().length() >= 2 ? request.getState().substring(0, 2).toUpperCase() : "IN";
            long count = projectRepository.count() + 1;
            projectCode = String.format("GD-%s-%d-%03d", stateCode, Year.now().getValue(), count);
        }

        Double baselineScore = request.getBaselineGapScore();
        if (baselineScore == null && village != null) {
            baselineScore = village.getGapScore();
        }

        Project project = Project.builder()
                .projectCode(projectCode)
                .projectName(request.getProjectName())
                .description(request.getDescription())
                .state(request.getState())
                .district(request.getDistrict())
                .block(request.getBlock() != null ? request.getBlock() : (village != null ? village.getBlock() : null))
                .village(village)
                .villageName(request.getVillageName() != null ? request.getVillageName() : (village != null ? village.getVillageName() : null))
                .latitude(request.getLatitude() != null ? request.getLatitude() : (village != null ? village.getLatitude() : null))
                .longitude(request.getLongitude() != null ? request.getLongitude() : (village != null ? village.getLongitude() : null))
                .projectType(request.getProjectType() != null ? request.getProjectType() : "Rural Infrastructure Enhancement")
                .priority(request.getPriority() != null ? request.getPriority() : PriorityLevel.HIGH)
                .status(request.getStatus() != null ? request.getStatus() : ProjectStatus.PLANNED)
                .budgetAllocated(request.getBudgetAllocated() != null ? request.getBudgetAllocated() : BigDecimal.ZERO)
                .budgetSpent(BigDecimal.ZERO)
                .baselineGapScore(baselineScore)
                .targetGapScore(request.getTargetGapScore() != null ? request.getTargetGapScore() : (baselineScore != null ? Math.max(10.0, baselineScore * 0.4) : 20.0))
                .currentGapScore(baselineScore)
                .gapReductionPct(0.0)
                .startDate(request.getStartDate())
                .targetDate(request.getTargetDate())
                .createdBy(performedByEmail)
                .deleted(false)
                .build();

        Project savedProject = projectRepository.save(project);

        // Associate initial requirements if provided
        if (request.getInitialRequirementIds() != null && !request.getInitialRequirementIds().isEmpty()) {
            for (Long reqId : request.getInitialRequirementIds()) {
                requirementRepository.findById(reqId).ifPresent(req -> {
                    req.setProject(savedProject);
                    req.setStatus(RequirementStatus.APPROVED);
                    requirementRepository.save(req);
                });
            }
        }

        auditLogService.logAction(null, performedByEmail, "PM", "PROJECT_CREATED", "PROJECT", savedProject.getId().toString(), null, "Created project: " + savedProject.getProjectCode(), null, null);

        return mapToDto(savedProject);
    }

    @Transactional
    public ProjectDto.ProjectResponseDto updateProject(Long id, ProjectDto.ProjectUpdateRequest request, String performedByEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (request.getProjectName() != null) project.setProjectName(request.getProjectName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getProjectType() != null) project.setProjectType(request.getProjectType());
        if (request.getPriority() != null) project.setPriority(request.getPriority());
        if (request.getBudgetAllocated() != null) project.setBudgetAllocated(request.getBudgetAllocated());
        if (request.getBudgetSpent() != null) project.setBudgetSpent(request.getBudgetSpent());
        if (request.getStartDate() != null) project.setStartDate(request.getStartDate());
        if (request.getTargetDate() != null) project.setTargetDate(request.getTargetDate());
        if (request.getCurrentGapScore() != null) project.setCurrentGapScore(request.getCurrentGapScore());
        if (request.getGapReductionPct() != null) project.setGapReductionPct(request.getGapReductionPct());
        if (request.getImpactSummary() != null) project.setImpactSummary(request.getImpactSummary());

        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
            if (request.getStatus() == ProjectStatus.COMPLETED) {
                project.setCompletedAt(LocalDateTime.now());
            }
        }

        Project updated = projectRepository.save(project);
        auditLogService.logAction(null, performedByEmail, "PM", "PROJECT_UPDATED", "PROJECT", updated.getId().toString(), null, "Updated project: " + updated.getProjectCode(), null, null);

        return mapToDto(updated);
    }

    @Transactional
    public ProjectDto.ProjectAssignmentDto assignUserToProject(Long projectId, ProjectDto.AssignUserRequest request, String performedByEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        ProjectAssignment assignment = projectAssignmentRepository.findByProjectIdAndUserId(projectId, user.getId())
                .orElse(ProjectAssignment.builder()
                        .project(project)
                        .user(user)
                        .build());

        assignment.setRoleInProject(request.getRoleInProject() != null ? request.getRoleInProject() : "FIELD_OFFICER");
        assignment.setAssignedBy(performedByEmail);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setStatus("ACTIVE");

        ProjectAssignment saved = projectAssignmentRepository.save(assignment);

        auditLogService.logAction(user.getId(), performedByEmail, "PM", "USER_ASSIGNED_TO_PROJECT", "PROJECT_ASSIGNMENT", saved.getId().toString(), null, "Assigned user " + user.getEmail() + " to project " + project.getProjectCode(), null, null);

        return mapAssignmentToDto(saved);
    }

    @Transactional
    public void deleteProject(Long id, String performedByEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        project.setDeleted(true);
        project.setDeletedAt(LocalDateTime.now());
        project.setDeletedBy(performedByEmail);
        projectRepository.save(project);

        auditLogService.logAction(null, performedByEmail, "PM", "PROJECT_DELETED", "PROJECT", id.toString(), null, "Soft deleted project: " + project.getProjectCode(), null, null);
    }

    @Transactional
    public void restoreProject(Long id, String performedByEmail) {
        projectRepository.restoreDeletedProject(id);
        auditLogService.logAction(null, performedByEmail, "SUPER_ADMIN", "PROJECT_RESTORED", "PROJECT", id.toString(), null, "Restored project id: " + id, null, null);
    }

    public ProjectDto.ProjectResponseDto mapToDto(Project project) {
        List<ProjectDto.ProjectAssignmentDto> assignments = projectAssignmentRepository.findByProjectId(project.getId()).stream()
                .map(this::mapAssignmentToDto)
                .collect(Collectors.toList());

        int reqsCount = requirementRepository.findByProjectId(project.getId()).size();
        int assetsCount = assetRepository.findByProjectId(project.getId()).size();

        return ProjectDto.ProjectResponseDto.builder()
                .id(project.getId())
                .projectCode(project.getProjectCode())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .state(project.getState())
                .district(project.getDistrict())
                .block(project.getBlock())
                .villageId(project.getVillage() != null ? project.getVillage().getId() : null)
                .villageName(project.getVillageName())
                .latitude(project.getLatitude())
                .longitude(project.getLongitude())
                .projectType(project.getProjectType())
                .priority(project.getPriority())
                .status(project.getStatus())
                .budgetAllocated(project.getBudgetAllocated())
                .budgetSpent(project.getBudgetSpent())
                .baselineGapScore(project.getBaselineGapScore())
                .targetGapScore(project.getTargetGapScore())
                .currentGapScore(project.getCurrentGapScore())
                .gapReductionPct(project.getGapReductionPct())
                .impactSummary(project.getImpactSummary())
                .startDate(project.getStartDate())
                .targetDate(project.getTargetDate())
                .completedAt(project.getCompletedAt())
                .createdBy(project.getCreatedBy())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .requirementsCount(reqsCount)
                .assetsCount(assetsCount)
                .assignments(assignments)
                .build();
    }

    private ProjectDto.ProjectAssignmentDto mapAssignmentToDto(ProjectAssignment assignment) {
        return ProjectDto.ProjectAssignmentDto.builder()
                .id(assignment.getId())
                .projectId(assignment.getProject().getId())
                .userId(assignment.getUser().getId())
                .userName(assignment.getUser().getName())
                .userEmail(assignment.getUser().getEmail())
                .roleInProject(assignment.getRoleInProject())
                .assignedBy(assignment.getAssignedBy())
                .assignedAt(assignment.getAssignedAt())
                .status(assignment.getStatus())
                .build();
    }
}
