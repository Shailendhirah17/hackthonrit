package in.gov.gramdrishti.dto;

import in.gov.gramdrishti.entity.PriorityLevel;
import in.gov.gramdrishti.entity.RequirementCategory;
import in.gov.gramdrishti.entity.RequirementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RequirementDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequirementCreateRequest {
        private Long projectId;
        private Long villageId;

        @NotNull(message = "Category is required")
        private RequirementCategory category;

        @NotBlank(message = "Title is required")
        private String title;

        private String description;
        private PriorityLevel priority = PriorityLevel.HIGH;
        private Double aiScore;
        private String source = "AI_SURVEY_ANALYSIS";
        private BigDecimal estimatedCost;
        private String suggestedScheme;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequirementUpdateRequest {
        private String title;
        private String description;
        private RequirementCategory category;
        private PriorityLevel priority;
        private RequirementStatus status;
        private BigDecimal estimatedCost;
        private String suggestedScheme;
        private Long projectId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequirementStatusUpdateRequest {
        @NotNull(message = "Status is required")
        private RequirementStatus status;

        private String comment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequirementResponseDto {
        private Long id;
        private Long projectId;
        private String projectCode;
        private String projectName;
        private Long villageId;
        private String villageName;
        private String state;
        private String district;
        private RequirementCategory category;
        private String title;
        private String description;
        private PriorityLevel priority;
        private Double aiScore;
        private String source;
        private RequirementStatus status;
        private BigDecimal estimatedCost;
        private String suggestedScheme;
        private String createdBy;
        private String approvedBy;
        private LocalDateTime approvedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
