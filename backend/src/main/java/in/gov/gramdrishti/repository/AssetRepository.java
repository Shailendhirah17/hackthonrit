package in.gov.gramdrishti.repository;

import in.gov.gramdrishti.entity.Asset;
import in.gov.gramdrishti.entity.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByProjectId(Long projectId);

    List<Asset> findByFolderId(Long folderId);

    List<Asset> findByProjectIdAndFolderIsNull(Long projectId);

    List<Asset> findByProjectIdAndIsFieldEvidenceTrue(Long projectId);

    @Query("SELECT a FROM Asset a WHERE a.deleted = false AND " +
           "(:projectId IS NULL OR a.project.id = :projectId) AND " +
           "(:folderId IS NULL OR a.folder.id = :folderId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:fileType IS NULL OR a.fileType = :fileType) AND " +
           "(:query IS NULL OR LOWER(a.fileName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Asset> filterAssets(
        @Param("projectId") Long projectId,
        @Param("folderId") Long folderId,
        @Param("status") AssetStatus status,
        @Param("fileType") String fileType,
        @Param("query") String query,
        Pageable pageable
    );

    @Modifying
    @Query(value = "UPDATE assets SET is_deleted = false, deleted_at = NULL, deleted_by = NULL WHERE id = :id", nativeQuery = true)
    void restoreDeletedAsset(@Param("id") Long id);
}
