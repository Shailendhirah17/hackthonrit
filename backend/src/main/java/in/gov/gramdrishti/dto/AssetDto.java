package in.gov.gramdrishti.dto;

import in.gov.gramdrishti.entity.AssetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public class AssetDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateFolderRequest {
        private Long projectId;
        private Long parentFolderId;

        @NotBlank(message = "Folder name is required")
        private String folderName;

        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FolderResponseDto {
        private Long id;
        private Long projectId;
        private Long parentFolderId;
        private String folderName;
        private String description;
        private String createdBy;
        private int subFoldersCount;
        private int assetsCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetResponseDto {
        private Long id;
        private Long projectId;
        private String projectCode;
        private String projectName;
        private Long folderId;
        private String fileName;
        private String fileType;
        private String mimeType;
        private Long fileSize;
        private String storageKey;
        private String checksumSha256;
        private Integer versionNumber;
        private AssetStatus status;
        private Double latitude;
        private Double longitude;
        private Boolean isFieldEvidence;
        private String aiVerificationStatus;
        private String uploadedBy;
        private String downloadUrl;
        private String viewUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<AssetVersionDto> versionHistory;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetVersionDto {
        private Long id;
        private Long assetId;
        private Integer versionNumber;
        private Long fileSize;
        private String checksumSha256;
        private String changeComment;
        private String uploadedBy;
        private LocalDateTime createdAt;
    }
}
