package in.gov.gramdrishti.dto;

import in.gov.gramdrishti.entity.PriorityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class VillageDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VillageResponseDto {
        private Long id;
        private String censusCode;
        private String villageName;
        private String state;
        private String district;
        private String block;
        private String gramPanchayat;
        private Double latitude;
        private Double longitude;
        private Integer population;
        private Integer householdCount;
        private Double gapScore;
        private Double adequacyScore;
        private PriorityLevel priority;
        private Double roadConnectivityIndex;
        private Double healthAccessIndex;
        private Double educationAccessIndex;
        private Double waterSanitationIndex;
        private Double digitalConnectivityIndex;
        private Double powerReliabilityIndex;
        private int infrastructureCount;
        private int activeProjectsCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
