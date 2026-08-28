package in.gov.gramdrishti.entity;

import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "villages", indexes = {
    @Index(name = "idx_village_state_district", columnList = "state,district"),
    @Index(name = "idx_village_gap_score", columnList = "gap_score"),
    @Index(name = "idx_village_priority", columnList = "priority")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE villages SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Village {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "census_code", length = 50)
    private String censusCode;

    @Column(name = "village_name", nullable = false, length = 150)
    private String villageName;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(length = 100)
    private String block;

    @Column(name = "gram_panchayat", length = 150)
    private String gramPanchayat;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Integer population;

    @Column(name = "household_count")
    private Integer householdCount;

    // AI Analytical Indicators
    @Column(name = "gap_score")
    @Builder.Default
    private Double gapScore = 50.0;

    @Column(name = "adequacy_score")
    @Builder.Default
    private Double adequacyScore = 50.0;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private PriorityLevel priority = PriorityLevel.MEDIUM;

    @Column(name = "road_connectivity_index")
    private Double roadConnectivityIndex;

    @Column(name = "health_access_index")
    private Double healthAccessIndex;

    @Column(name = "education_access_index")
    private Double educationAccessIndex;

    @Column(name = "water_sanitation_index")
    private Double waterSanitationIndex;

    @Column(name = "digital_connectivity_index")
    private Double digitalConnectivityIndex;

    @Column(name = "power_reliability_index")
    private Double powerReliabilityIndex;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
