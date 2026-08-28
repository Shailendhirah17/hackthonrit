package in.gov.gramdrishti.repository;

import in.gov.gramdrishti.entity.InfraStatus;
import in.gov.gramdrishti.entity.InfraType;
import in.gov.gramdrishti.entity.Infrastructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfrastructureRepository extends JpaRepository<Infrastructure, Long> {

    List<Infrastructure> findByVillageId(Long villageId);

    List<Infrastructure> findByInfraType(InfraType infraType);

    List<Infrastructure> findByStatus(InfraStatus status);

    @Query("SELECT i FROM Infrastructure i WHERE i.deleted = false AND " +
           "(:infraType IS NULL OR i.infraType = :infraType) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:villageId IS NULL OR i.village.id = :villageId)")
    List<Infrastructure> filterInfrastructure(
        @Param("infraType") InfraType infraType,
        @Param("status") InfraStatus status,
        @Param("villageId") Long villageId
    );

    @Modifying
    @Query(value = "UPDATE infrastructure SET is_deleted = false, deleted_at = NULL, deleted_by = NULL WHERE id = :id", nativeQuery = true)
    void restoreDeletedInfrastructure(@Param("id") Long id);
}
