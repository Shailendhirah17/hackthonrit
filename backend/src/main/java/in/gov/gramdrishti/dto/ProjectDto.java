package in.gov.gramdrishti.dto;

import in.gov.gramdrishti.entity.PriorityLevel;
import in.gov.gramdrishti.entity.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectCreateRequest {
        @NotBlank(message = "Project name is required")
        private String projectName;

        private String projectCode;
        private String description;

        @NotBlank(message = "State is required")
        private String state;

        @NotBlank(message = "District is required")
        private String district;

        private String block;
        private Long villageId;
        private String villageName;
        private Double latitude;
        private Double longitude;
        private String projectType;

        @NotNull(message = "Priority is required")
        private PriorityLevel priority;

        private ProjectStatus status = ProjectStatus.DRAFT;
        private BigDecimal budgetAllocated;
        private Double baselineGapScore;
        private Double targetGapScore;
        private LocalDate startDate;
        private LocalDate targetDate;
        private List<Long> initialRequirementIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectUpdateRequest {
        private String projectName;
        private String description;
        private String projectType;
        private PriorityLevel priority;
        private ProjectStatus status;
        private BigDecimal budgetAllocated;
        private BigDecimal budgetSpent;
        private Double currentGapScore;
        private Double gapReductionPct;
        private String impactSummary;
        private LocalDate startDate;
        private LocalDate targetDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectResponseDto {
        private Long id;
        private String projectCode;
        private String projectName;
        private String description;
        private String state;
        private String district;
        private String block;
        private Long villageId;
        private String villageName;
        private Double latitude;
        private Double longitude;
        private String projectType;
        private PriorityLevel priority;
        private ProjectStatus status;
        private BigDecimal budgetAllocated;
        private BigDecimal budgetSpent;
        private Double baselineGapScore;
        private Double targetGapScore;
        private Double currentGapScore;
        private Double gapReductionPct;
        private String impactSummary;
        private LocalDate startDate;
        private LocalDate targetDate;
        private LocalDateTime completedAt;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int requirementsCount;
        private int assetsCount;
        private List<ProjectAssignmentDto> assignments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectAssignmentDto {
        private Long id;
        private Long projectId;
        private Long userId;
        private String userName;
        private String userEmail;
        private String roleInProject;
        private String assignedBy;
        private LocalDateTime assignedAt;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignUserRequest {
        @NotNull(message = "User ID is required")
        private Long userId;

        private String roleInProject;
    }
}
