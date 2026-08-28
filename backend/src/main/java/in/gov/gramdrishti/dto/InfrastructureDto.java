package in.gov.gramdrishti.dto;

import in.gov.gramdrishti.entity.InfraStatus;
import in.gov.gramdrishti.entity.InfraType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class InfrastructureDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InfrastructureCreateRequest {
        @NotNull(message = "Village ID is required")
        private Long villageId;

        @NotNull(message = "Infrastructure Type is required")
        private InfraType infraType;

        @NotBlank(message = "Name is required")
        private String name;

        private InfraStatus status = InfraStatus.OPERATIONAL;
        private Double conditionScore = 7.0;

        @NotNull(message = "Latitude is required")
        private Double latitude;

        @NotNull(message = "Longitude is required")
        private Double longitude;

        private String capacityOrLength;
        private Integer establishedYear;
        private String schemeName;
        private String attributesJson;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InfrastructureResponseDto {
        private Long id;
        private Long villageId;
        private String villageName;
        private String state;
        private String district;
        private InfraType infraType;
        private String name;
        private InfraStatus status;
        private Double conditionScore;
        private Double latitude;
        private Double longitude;
        private String capacityOrLength;
        private Integer establishedYear;
        private String schemeName;
        private String attributesJson;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
