package in.gov.gramdrishti.repository;

import in.gov.gramdrishti.entity.AssetVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetVersionRepository extends JpaRepository<AssetVersion, Long> {
    List<AssetVersion> findByAssetIdOrderByVersionNumberDesc(Long assetId);
}
