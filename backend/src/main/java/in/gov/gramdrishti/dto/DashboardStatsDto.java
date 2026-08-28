package in.gov.gramdrishti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalVillages;
    private long criticalVillagesCount;
    private long highPriorityVillagesCount;
    private long totalProjects;
    private long activeProjectsCount;
    private long completedProjectsCount;
    private long totalRequirements;
    private long approvedRequirementsCount;
    private BigDecimal totalBudgetAllocated;
    private BigDecimal totalBudgetSpent;
    private Double averageGapReductionPct;
    private Double averageNationalGapScore;
    private Map<String, Long> projectsByStatus;
    private Map<String, Long> villagesByPriority;
    private Map<String, Double> stateAverageGapScores;
    private List<Map<String, Object>> highDeficitVillages;
    private List<Map<String, Object>> recentImpactProjects;
}
