package in.gov.gramdrishti.repository;

import in.gov.gramdrishti.entity.PriorityLevel;
import in.gov.gramdrishti.entity.Requirement;
import in.gov.gramdrishti.entity.RequirementCategory;
import in.gov.gramdrishti.entity.RequirementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, Long> {

    List<Requirement> findByProjectId(Long projectId);

    List<Requirement> findByVillageId(Long villageId);

    List<Requirement> findByStatus(RequirementStatus status);

    @Query("SELECT r FROM Requirement r WHERE r.deleted = false AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:category IS NULL OR r.category = :category) AND " +
           "(:priority IS NULL OR r.priority = :priority) AND " +
           "(:projectId IS NULL OR r.project.id = :projectId) AND " +
           "(:villageId IS NULL OR r.village.id = :villageId) AND " +
           "(:query IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Requirement> filterRequirements(
        @Param("status") RequirementStatus status,
        @Param("category") RequirementCategory category,
        @Param("priority") PriorityLevel priority,
        @Param("projectId") Long projectId,
        @Param("villageId") Long villageId,
        @Param("query") String query,
        Pageable pageable
    );

    @Query("SELECT COUNT(r) FROM Requirement r WHERE r.deleted = false AND r.status = :status")
    long countByStatus(@Param("status") RequirementStatus status);

    @Modifying
    @Query(value = "UPDATE requirements SET is_deleted = false, deleted_at = NULL WHERE id = :id", nativeQuery = true)
    void restoreDeletedRequirement(@Param("id") Long id);
}
