package in.gov.gramdrishti.repository;

import in.gov.gramdrishti.entity.PriorityLevel;
import in.gov.gramdrishti.entity.Village;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VillageRepository extends JpaRepository<Village, Long> {

    List<Village> findByStateAndDistrict(String state, String district);

    List<Village> findByPriority(PriorityLevel priority);

    @Query("SELECT v FROM Village v WHERE v.deleted = false AND " +
           "(:state IS NULL OR v.state = :state) AND " +
           "(:district IS NULL OR v.district = :district) AND " +
           "(:priority IS NULL OR v.priority = :priority) AND " +
           "(:query IS NULL OR LOWER(v.villageName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(v.block) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Village> filterVillages(
        @Param("state") String state,
        @Param("district") String district,
        @Param("priority") PriorityLevel priority,
        @Param("query") String query,
        Pageable pageable
    );

    @Query("SELECT v.state, COUNT(v), AVG(v.gapScore) FROM Village v WHERE v.deleted = false GROUP BY v.state")
    List<Object[]> getStateLevelAggregates();

    @Query("SELECT COUNT(v) FROM Village v WHERE v.deleted = false AND v.priority = 'CRITICAL'")
    long countCriticalVillages();

    @Modifying
    @Query(value = "UPDATE villages SET is_deleted = false, deleted_at = NULL, deleted_by = NULL WHERE id = :id", nativeQuery = true)
    void restoreDeletedVillage(@Param("id") Long id);
}
