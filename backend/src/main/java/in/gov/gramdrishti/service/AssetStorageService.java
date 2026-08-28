package in.gov.gramdrishti.service;

import in.gov.gramdrishti.dto.AssetDto;
import in.gov.gramdrishti.entity.*;
import in.gov.gramdrishti.exception.BadRequestException;
import in.gov.gramdrishti.exception.ResourceNotFoundException;
import in.gov.gramdrishti.repository.*;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetStorageService {

    private final MinioClient minioClient;
    private final AssetRepository assetRepository;
    private final AssetVersionRepository assetVersionRepository;
    private final FolderRepository folderRepository;
    private final ProjectRepository projectRepository;
    private final AuditLogService auditLogService;

    @Value("${app.minio.bucket-name:gramdrishti-assets}")
    private String bucketName;

    @Value("${app.minio.expiry-seconds:3600}")
    private int expirySeconds;

    private final Path localFallbackDir = Paths.get(System.getProperty("user.home"), ".gramdrishti", "storage");

    @PostConstruct
    public void initBucket() {
        try {
            Files.createDirectories(localFallbackDir);
            if (minioClient != null) {
                boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                if (!found) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    log.info("MinIO bucket '{}' created successfully.", bucketName);
                }
            }
        } catch (Exception e) {
            log.warn("MinIO bucket initialization note (fallback mode active): {}", e.getMessage());
        }
    }

    @Transactional
    public AssetDto.AssetResponseDto uploadAsset(
            Long projectId,
            Long folderId,
            MultipartFile file,
            String fileType,
            Double latitude,
            Double longitude,
            Boolean isFieldEvidence,
            String performedByEmail
    ) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        Project project = null;
        if (projectId != null) {
            project = projectRepository.findById(projectId).orElse(null);
        }

        Folder folder = null;
        if (folderId != null) {
            folder = folderRepository.findById(folderId).orElse(null);
        }

        try {
            byte[] fileBytes = file.getBytes();
            String sha256 = DigestUtils.sha256Hex(fileBytes);
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file_" + System.currentTimeMillis();
            String extension = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String storageKey = String.format("%s/%s_%s%s",
                    project != null ? project.getProjectCode() : "general",
                    UUID.randomUUID().toString().substring(0, 8),
                    System.currentTimeMillis(),
                    extension
            );

            // Upload to MinIO or fallback locally
            storeFile(storageKey, file.getInputStream(), file.getSize(), file.getContentType());

            Asset asset = Asset.builder()
                    .project(project)
                    .folder(folder)
                    .fileName(originalFilename)
                    .fileType(fileType != null ? fileType : inferFileType(originalFilename, file.getContentType()))
                    .mimeType(file.getContentType())
                    .fileSize(file.getSize())
                    .storageKey(storageKey)
                    .checksumSha256(sha256)
                    .versionNumber(1)
                    .status(AssetStatus.ACTIVE)
                    .latitude(latitude)
                    .longitude(longitude)
                    .isFieldEvidence(isFieldEvidence != null ? isFieldEvidence : false)
                    .aiVerificationStatus(isFieldEvidence != null && isFieldEvidence ? "PENDING_VERIFICATION" : "NOT_APPLICABLE")
                    .uploadedBy(performedByEmail)
                    .deleted(false)
                    .build();

            Asset savedAsset = assetRepository.save(asset);

            // Save initial version
            AssetVersion version = AssetVersion.builder()
                    .asset(savedAsset)
                    .versionNumber(1)
                    .storageKey(storageKey)
                    .fileSize(file.getSize())
                    .checksumSha256(sha256)
                    .changeComment("Initial upload")
                    .uploadedBy(performedByEmail)
                    .build();
            assetVersionRepository.save(version);

            auditLogService.logAction(null, performedByEmail, "USER", "FILE_UPLOADED", "ASSET", savedAsset.getId().toString(), null, "Uploaded file " + originalFilename + " (" + sha256 + ")", null, null);

            return mapToDto(savedAsset);
        } catch (Exception e) {
            log.error("Failed to upload asset: ", e);
            throw new BadRequestException("Failed to upload asset: " + e.getMessage());
        }
    }

    @Transactional
    public AssetDto.AssetResponseDto uploadNewVersion(Long assetId, MultipartFile file, String comment, String performedByEmail) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + assetId));

        try {
            byte[] fileBytes = file.getBytes();
            String sha256 = DigestUtils.sha256Hex(fileBytes);
            int newVersionNum = asset.getVersionNumber() + 1;
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : asset.getFileName();
            String extension = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String storageKey = String.format("%s/v%d_%s_%s%s",
                    asset.getProject() != null ? asset.getProject().getProjectCode() : "general",
                    newVersionNum,
                    UUID.randomUUID().toString().substring(0, 8),
                    System.currentTimeMillis(),
                    extension
            );

            storeFile(storageKey, file.getInputStream(), file.getSize(), file.getContentType());

            asset.setStorageKey(storageKey);
            asset.setFileSize(file.getSize());
            asset.setChecksumSha256(sha256);
            asset.setVersionNumber(newVersionNum);
            asset.setUpdatedAt(LocalDateTime.now());
            Asset updatedAsset = assetRepository.save(asset);

            AssetVersion version = AssetVersion.builder()
                    .asset(updatedAsset)
                    .versionNumber(newVersionNum)
                    .storageKey(storageKey)
                    .fileSize(file.getSize())
                    .checksumSha256(sha256)
                    .changeComment(comment != null ? comment : "Version " + newVersionNum + " update")
                    .uploadedBy(performedByEmail)
                    .build();
            assetVersionRepository.save(version);

            auditLogService.logAction(null, performedByEmail, "USER", "FILE_NEW_VERSION_UPLOADED", "ASSET", asset.getId().toString(), null, "Uploaded version " + newVersionNum + " for " + asset.getFileName(), null, null);

            return mapToDto(updatedAsset);
        } catch (Exception e) {
            log.error("Failed to upload new asset version: ", e);
            throw new BadRequestException("Failed to upload new asset version: " + e.getMessage());
        }
    }

    private void storeFile(String storageKey, InputStream stream, long size, String contentType) throws Exception {
        if (minioClient != null) {
            try {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(storageKey)
                                .stream(stream, size, -1)
                                .contentType(contentType)
                                .build()
                );
                return;
            } catch (Exception e) {
                log.warn("MinIO putObject error, saving to local fallback: {}", e.getMessage());
            }
        }
        // Fallback local storage
        Path targetPath = localFallbackDir.resolve(storageKey.replace("/", "_"));
        Files.copy(stream, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public String generatePresignedUrl(String storageKey, boolean download, String fileName) {
        if (minioClient != null) {
            try {
                return minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(bucketName)
                                .object(storageKey)
                                .expiry(expirySeconds, TimeUnit.SECONDS)
                                .extraQueryParams(download ? java.util.Collections.singletonMap("response-content-disposition", "attachment; filename=\"" + fileName + "\"") : null)
                                .build()
                );
            } catch (Exception e) {
                log.debug("MinIO presigned URL fallback: {}", e.getMessage());
            }
        }
        return "/api/assets/download-raw?key=" + storageKey;
    }

    @Transactional(readOnly = true)
    public Page<AssetDto.AssetResponseDto> getAssets(Long projectId, Long folderId, AssetStatus status, String fileType, String query, Pageable pageable) {
        return assetRepository.filterAssets(projectId, folderId, status, fileType, query, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public AssetDto.AssetResponseDto getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));
        return mapToDto(asset);
    }

    @Transactional
    public void deleteAsset(Long id, String performedByEmail) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));

        asset.setDeleted(true);
        asset.setDeletedAt(LocalDateTime.now());
        asset.setDeletedBy(performedByEmail);
        asset.setStatus(AssetStatus.DELETED);
        assetRepository.save(asset);

        auditLogService.logAction(null, performedByEmail, "PM", "FILE_DELETED", "ASSET", id.toString(), null, "Deleted asset: " + asset.getFileName(), null, null);
    }

    // Folders
    @Transactional
    public AssetDto.FolderResponseDto createFolder(AssetDto.CreateFolderRequest request, String performedByEmail) {
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId()).orElse(null);
        }

        Folder parentFolder = null;
        if (request.getParentFolderId() != null) {
            parentFolder = folderRepository.findById(request.getParentFolderId()).orElse(null);
        }

        Folder folder = Folder.builder()
                .project(project)
                .parentFolder(parentFolder)
                .folderName(request.getFolderName())
                .description(request.getDescription())
                .createdBy(performedByEmail)
                .deleted(false)
                .build();

        Folder saved = folderRepository.save(folder);
        return mapFolderToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AssetDto.FolderResponseDto> getProjectFolders(Long projectId) {
        return folderRepository.findByProjectId(projectId).stream()
                .filter(f -> !f.isDeleted())
                .map(this::mapFolderToDto)
                .collect(Collectors.toList());
    }

    private String inferFileType(String filename, String mimeType) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
            return "IMAGE";
        } else if (lower.endsWith(".geojson") || lower.endsWith(".kml") || lower.endsWith(".shp") || lower.endsWith(".tif")) {
            return "GIS_SPATIAL_DATA";
        } else if (lower.endsWith(".pdf")) {
            return "REPORT_DOCUMENT";
        } else {
            return "DATA_FILE";
        }
    }

    public AssetDto.AssetResponseDto mapToDto(Asset asset) {
        String downloadUrl = generatePresignedUrl(asset.getStorageKey(), true, asset.getFileName());
        String viewUrl = generatePresignedUrl(asset.getStorageKey(), false, asset.getFileName());

        List<AssetDto.AssetVersionDto> versions = assetVersionRepository.findByAssetIdOrderByVersionNumberDesc(asset.getId()).stream()
                .map(v -> AssetDto.AssetVersionDto.builder()
                        .id(v.getId())
                        .assetId(v.getAsset().getId())
                        .versionNumber(v.getVersionNumber())
                        .fileSize(v.getFileSize())
                        .checksumSha256(v.getChecksumSha256())
                        .changeComment(v.getChangeComment())
                        .uploadedBy(v.getUploadedBy())
                        .createdAt(v.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return AssetDto.AssetResponseDto.builder()
                .id(asset.getId())
                .projectId(asset.getProject() != null ? asset.getProject().getId() : null)
                .projectCode(asset.getProject() != null ? asset.getProject().getProjectCode() : null)
                .projectName(asset.getProject() != null ? asset.getProject().getProjectName() : null)
                .folderId(asset.getFolder() != null ? asset.getFolder().getId() : null)
                .fileName(asset.getFileName())
                .fileType(asset.getFileType())
                .mimeType(asset.getMimeType())
                .fileSize(asset.getFileSize())
                .storageKey(asset.getStorageKey())
                .checksumSha256(asset.getChecksumSha256())
                .versionNumber(asset.getVersionNumber())
                .status(asset.getStatus())
                .latitude(asset.getLatitude())
                .longitude(asset.getLongitude())
                .isFieldEvidence(asset.getIsFieldEvidence())
                .aiVerificationStatus(asset.getAiVerificationStatus())
                .uploadedBy(asset.getUploadedBy())
                .downloadUrl(downloadUrl)
                .viewUrl(viewUrl)
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .versionHistory(versions)
                .build();
    }

    public AssetDto.FolderResponseDto mapFolderToDto(Folder folder) {
        int subCount = folder.getSubFolders() != null ? folder.getSubFolders().size() : 0;
        int assetsCount = assetRepository.findByFolderId(folder.getId()).size();

        return AssetDto.FolderResponseDto.builder()
                .id(folder.getId())
                .projectId(folder.getProject() != null ? folder.getProject().getId() : null)
                .parentFolderId(folder.getParentFolder() != null ? folder.getParentFolder().getId() : null)
                .folderName(folder.getFolderName())
                .description(folder.getDescription())
                .createdBy(folder.getCreatedBy())
                .subFoldersCount(subCount)
                .assetsCount(assetsCount)
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }
}
