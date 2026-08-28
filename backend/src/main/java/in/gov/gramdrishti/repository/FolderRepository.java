package in.gov.gramdrishti.repository;

import in.gov.gramdrishti.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findByProjectIdAndParentFolderIsNull(Long projectId);

    List<Folder> findByParentFolderId(Long parentFolderId);

    List<Folder> findByProjectId(Long projectId);
}
