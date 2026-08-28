package in.gov.gramdrishti.controller;

import in.gov.gramdrishti.dto.ApiResponse;
import in.gov.gramdrishti.dto.UserDto;
import in.gov.gramdrishti.entity.UserStatus;
import in.gov.gramdrishti.security.UserPrincipal;
import in.gov.gramdrishti.service.UserService;
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

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "CRUD operations on users with RBAC enforcement")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "List and search users with pagination (Super Admin & Admin)")
    public ResponseEntity<ApiResponse<Page<UserDto.UserResponseDto>>> getUsers(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<UserDto.UserResponseDto> users = userService.getUsers(query, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'ANALYST', 'FIELD_OFFICER', 'VIEWER')")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserDto.UserResponseDto>> getUserById(@PathVariable Long id) {
        UserDto.UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Create a new user account (Super Admin & Admin)")
    public ResponseEntity<ApiResponse<UserDto.UserResponseDto>> createUser(
            @Valid @RequestBody UserDto.UserCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserDto.UserResponseDto created = userService.createUser(request, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("User created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update user details (Super Admin & Admin)")
    public ResponseEntity<ApiResponse<UserDto.UserResponseDto>> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto.UserUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserDto.UserResponseDto updated = userService.updateUser(id, request, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("User updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Change user status (ACTIVE, INACTIVE, SUSPENDED)")
    public ResponseEntity<ApiResponse<UserDto.UserResponseDto>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserDto.UserResponseDto updated = userService.updateUserStatus(id, status, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("User status updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Soft delete a user (Super Admin & Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        userService.deleteUser(id, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("User soft-deleted successfully", null));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Restore a soft-deleted user (Super Admin only)")
    public ResponseEntity<ApiResponse<Void>> restoreUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        userService.restoreUser(id, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.ok(ApiResponse.ok("User restored successfully", null));
    }
}
