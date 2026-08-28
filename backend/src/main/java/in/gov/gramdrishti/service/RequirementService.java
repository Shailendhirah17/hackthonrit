package in.gov.gramdrishti.service;

import in.gov.gramdrishti.dto.RequirementDto;
import in.gov.gramdrishti.entity.*;
import in.gov.gramdrishti.exception.ResourceNotFoundException;
import in.gov.gramdrishti.repository.ProjectRepository;
import in.gov.gramdrishti.repository.RequirementRepository;
import in.gov.gramdrishti.repository.VillageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RequirementService {

    private final RequirementRepository requirementRepository;
    private final ProjectRepository projectRepository;
    private final VillageRepository villageRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<RequirementDto.RequirementResponseDto> getRequirements(RequirementStatus status, RequirementCategory category, PriorityLevel priority, Long projectId, Long villageId, String query, Pageable pageable) {
        return requirementRepository.filterRequirements(status, category, priority, projectId, villageId, query, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public RequirementDto.RequirementResponseDto getRequirementById(Long id) {
        Requirement req = requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with id: " + id));
        return mapToDto(req);
    }

    @Transactional
    public RequirementDto.RequirementResponseDto createRequirement(RequirementDto.RequirementCreateRequest request, String performedByEmail) {
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId()).orElse(null);
        }

        Village village = null;
        if (request.getVillageId() != null) {
            village = villageRepository.findById(request.getVillageId()).orElse(null);
        } else if (project != null && project.getVillage() != null) {
            village = project.getVillage();
        }

        Requirement requirement = Requirement.builder()
                .project(project)
                .village(village)
                .category(request.getCategory())
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : PriorityLevel.HIGH)
                .aiScore(request.getAiScore() != null ? request.getAiScore() : 75.0)
                .source(request.getSource() != null ? request.getSource() : "AI_SURVEY_ANALYSIS")
                .status(RequirementStatus.IDENTIFIED)
                .estimatedCost(request.getEstimatedCost())
                .suggestedScheme(request.getSuggestedScheme())
                .createdBy(performedByEmail)
                .deleted(false)
                .build();

        Requirement saved = requirementRepository.save(requirement);

        auditLogService.logAction(null, performedByEmail, "ANALYST", "REQUIREMENT_CREATED", "REQUIREMENT", saved.getId().toString(), null, "Created requirement: " + saved.getTitle(), null, null);

        return mapToDto(saved);
    }

    @Transactional
    public RequirementDto.RequirementResponseDto updateRequirement(Long id, RequirementDto.RequirementUpdateRequest request, String performedByEmail) {
        Requirement req = requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with id: " + id));

        if (request.getTitle() != null) req.setTitle(request.getTitle());
        if (request.getDescription() != null) req.setDescription(request.getDescription());
        if (request.getCategory() != null) req.setCategory(request.getCategory());
        if (request.getPriority() != null) req.setPriority(request.getPriority());
        if (request.getEstimatedCost() != null) req.setEstimatedCost(request.getEstimatedCost());
        if (request.getSuggestedScheme() != null) req.setSuggestedScheme(request.getSuggestedScheme());
        if (request.getStatus() != null) req.setStatus(request.getStatus());

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId()).orElse(null);
            req.setProject(project);
        }

        Requirement updated = requirementRepository.save(req);
        auditLogService.logAction(null, performedByEmail, "PM", "REQUIREMENT_UPDATED", "REQUIREMENT", updated.getId().toString(), null, "Updated requirement: " + updated.getTitle(), null, null);

        return mapToDto(updated);
    }

    @Transactional
    public RequirementDto.RequirementResponseDto updateRequirementStatus(Long id, RequirementDto.RequirementStatusUpdateRequest request, String performedByEmail) {
        Requirement req = requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with id: " + id));

        req.setStatus(request.getStatus());
        if (request.getStatus() == RequirementStatus.APPROVED) {
            req.setApprovedBy(performedByEmail);
            req.setApprovedAt(LocalDateTime.now());
        }

        Requirement updated = requirementRepository.save(req);
        auditLogService.logAction(null, performedByEmail, "PM", "REQUIREMENT_STATUS_CHANGED", "REQUIREMENT", updated.getId().toString(), null, "Status updated to " + request.getStatus(), null, null);

        return mapToDto(updated);
    }

    @Transactional
    public void deleteRequirement(Long id, String performedByEmail) {
        Requirement req = requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with id: " + id));

        req.setDeleted(true);
        req.setDeletedAt(LocalDateTime.now());
        requirementRepository.save(req);

        auditLogService.logAction(null, performedByEmail, "PM", "REQUIREMENT_DELETED", "REQUIREMENT", id.toString(), null, "Deleted requirement: " + req.getTitle(), null, null);
    }

    public RequirementDto.RequirementResponseDto mapToDto(Requirement req) {
        return RequirementDto.RequirementResponseDto.builder()
                .id(req.getId())
                .projectId(req.getProject() != null ? req.getProject().getId() : null)
                .projectCode(req.getProject() != null ? req.getProject().getProjectCode() : null)
                .projectName(req.getProject() != null ? req.getProject().getProjectName() : null)
                .villageId(req.getVillage() != null ? req.getVillage().getId() : null)
                .villageName(req.getVillage() != null ? req.getVillage().getVillageName() : null)
                .state(req.getVillage() != null ? req.getVillage().getState() : (req.getProject() != null ? req.getProject().getState() : null))
                .district(req.getVillage() != null ? req.getVillage().getDistrict() : (req.getProject() != null ? req.getProject().getDistrict() : null))
                .category(req.getCategory())
                .title(req.getTitle())
                .description(req.getDescription())
                .priority(req.getPriority())
                .aiScore(req.getAiScore())
                .source(req.getSource())
                .status(req.getStatus())
                .estimatedCost(req.getEstimatedCost())
                .suggestedScheme(req.getSuggestedScheme())
                .createdBy(req.getCreatedBy())
                .approvedBy(req.getApprovedBy())
                .approvedAt(req.getApprovedAt())
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .build();
    }
}
