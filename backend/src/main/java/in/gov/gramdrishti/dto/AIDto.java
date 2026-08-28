package in.gov.gramdrishti.dto;

import in.gov.gramdrishti.entity.PriorityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class AIDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AIGapScoreRequest {
        private String villageId;
        private String villageName;
        private String state;
        private String district;
        private String block;
        private Integer population;
        private Double distanceToNearestAllWeatherRoadKm;
        private Double roadConditionIndex;
        private Double distanceToPhcKm;
        private Boolean subCentreAvailable;
        private Boolean hasPrimarySchool;
        private Double distanceToSecondarySchoolKm;
        private Double pupilTeacherRatio;
        private Double tapWaterCoveragePct;
        private Double waterQualityIndex;
        private Double mobile4g5gCoveragePct;
        private Double avgPowerSupplyHoursPerDay;
        private Double distanceToApmcMandiKm;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AICategoryBreakdown {
        private String category;
        private Double gapScore;
        private Double adequacyScore;
        private String status;
        private List<String> keyFindings;
        private List<String> recommendedInterventions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AIGapScoreResponse {
        private String villageName;
        private String district;
        private String state;
        private Double overallGapScore;
        private Double overallAdequacyScore;
        private PriorityLevel priority;
        private Double confidenceScore;
        private Map<String, Double> dimensionScores;
        private List<AICategoryBreakdown> breakdown;
        private List<Map<String, Object>> topRecommendations;
        private String analyzedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AIDetectionBBox {
        private String label;
        private Double confidence;
        private List<Integer> bbox;
        private String category;
        private String conditionRating;
        private Double estimatedLengthM;
        private Double areaSqM;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AIDetectionResponse {
        private String imageFilename;
        private String imageHash;
        private int detectedObjectsCount;
        private List<AIDetectionBBox> detections;
        private List<String> detectedInfrastructures;
        private List<String> identifiedDeficits;
        private Double potholeDensityPerKm;
        private String roadPavementType;
        private Double solarRooftopPotentialKw;
        private String processedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AIReEvaluationRequest {
        private String projectId;
        private String projectName;
        private String villageName;
        private String district;
        private Double baselineGapScore;
        private List<String> interventionsCompleted;
        private Map<String, Object> updatedMetrics;
        private List<String> evidenceAssetIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AIReEvaluationResponse {
        private String projectId;
        private String villageName;
        private Double baselineGapScore;
        private Double updatedGapScore;
        private Double gapReductionPct;
        private Double absoluteImprovementPoints;
        private String impactTier;
        private Map<String, Map<String, Double>> beforeVsAfterDimensions;
        private String impactSummary;
        private String verificationStatus;
        private String evaluatedAt;
    }
}
