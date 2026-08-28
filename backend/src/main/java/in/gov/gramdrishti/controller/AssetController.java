package in.gov.gramdrishti.controller;

import in.gov.gramdrishti.dto.ApiResponse;
import in.gov.gramdrishti.dto.AssetDto;
import in.gov.gramdrishti.entity.AssetStatus;
import in.gov.gramdrishti.security.UserPrincipal;
import in.gov.gramdrishti.service.AssetStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Tag(name = "Digital Asset Management (DAM)", description = "MinIO/S3 file storage, versioning, and presigned URLs")
public class AssetController {

    private final AssetStorageService assetStorageService;

    @GetMapping
    @Operation(summary = "List and search digital assets with pagination")
    public ResponseEntity<ApiResponse<Page<AssetDto.AssetResponseDto>>> getAssets(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<AssetDto.AssetResponseDto> assets = assetStorageService.getAssets(projectId, folderId, status, fileType, query, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.ok(assets));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get asset details by ID with temporary presigned URLs")
    public ResponseEntity<ApiResponse<AssetDto.AssetResponseDto>> getAssetById(@PathVariable Long id) {
        AssetDto.AssetResponseDto asset = assetStorageService.getAssetById(id);
        return ResponseEntity.ok(ApiResponse.ok(asset));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'ANALYST', 'FIELD_OFFICER')")
    @Operation(summary = "Upload a new digital asset (MinIO/S3 + SHA-256 Checksum)")
    public ResponseEntity<ApiResponse<AssetDto.AssetResponseDto>> uploadAsset(
            @RequestParam(value = "file") MultipartFile file,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "folderId", required = false) Long folderId,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "isFieldEvidence", required = false, defaultValue = "false") Boolean isFieldEvidence,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AssetDto.AssetResponseDto uploaded = assetStorageService.uploadAsset(
                projectId, folderId, file, fileType, latitude, longitude, isFieldEvidence,
                principal != null ? principal.getEmail() : "SYSTEM"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("File uploaded successfully", uploaded));
    }

    @PostMapping(value = "/{id}/version", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'ANALYST', 'FIELD_OFFICER')")
    @Operation(summary = "Upload a new version of an existing asset")
    public ResponseEntity<ApiResponse<AssetDto.AssetResponseDto>> uploadNewVersion(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "comment", required = false) String comment,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AssetDto.AssetResponseDto updated = assetStorageService.uploadNewVersion(id, file, comment, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("New version uploaded successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Delete an asset (Super Admin, Admin, PM)")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        assetStorageService.deleteAsset(id, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("Asset deleted successfully", null));
    }

    // Folder Endpoints
    @PostMapping("/folders")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'ANALYST', 'FIELD_OFFICER')")
    @Operation(summary = "Create a project folder")
    public ResponseEntity<ApiResponse<AssetDto.FolderResponseDto>> createFolder(
            @Valid @RequestBody AssetDto.CreateFolderRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AssetDto.FolderResponseDto folder = assetStorageService.createFolder(request, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Folder created successfully", folder));
    }

    @GetMapping("/folders/project/{projectId}")
    @Operation(summary = "Get folder hierarchy for a project")
    public ResponseEntity<ApiResponse<List<AssetDto.FolderResponseDto>>> getProjectFolders(@PathVariable Long projectId) {
        List<AssetDto.FolderResponseDto> folders = assetStorageService.getProjectFolders(projectId);
        return ResponseEntity.ok(ApiResponse.ok(folders));
    }
}
