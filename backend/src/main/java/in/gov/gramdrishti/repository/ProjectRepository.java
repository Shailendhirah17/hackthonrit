package in.gov.gramdrishti.repository;

import in.gov.gramdrishti.entity.PriorityLevel;
import in.gov.gramdrishti.entity.Project;
import in.gov.gramdrishti.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByProjectCode(String projectCode);

    boolean existsByProjectCode(String projectCode);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByVillageId(Long villageId);

    @Query("SELECT p FROM Project p WHERE p.deleted = false AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:priority IS NULL OR p.priority = :priority) AND " +
           "(:state IS NULL OR p.state = :state) AND " +
           "(:district IS NULL OR p.district = :district) AND " +
           "(:query IS NULL OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.projectCode) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Project> filterProjects(
        @Param("status") ProjectStatus status,
        @Param("priority") PriorityLevel priority,
        @Param("state") String state,
        @Param("district") String district,
        @Param("query") String query,
        Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM Project p WHERE p.deleted = false AND p.status = :status")
    long countByStatus(@Param("status") ProjectStatus status);

    @Query("SELECT AVG(p.gapReductionPct) FROM Project p WHERE p.deleted = false AND p.status = 'COMPLETED' AND p.gapReductionPct IS NOT NULL")
    Double getAverageImpactGapReduction();

    @Modifying
    @Query(value = "UPDATE projects SET is_deleted = false, deleted_at = NULL, deleted_by = NULL WHERE id = :id", nativeQuery = true)
    void restoreDeletedProject(@Param("id") Long id);
}
