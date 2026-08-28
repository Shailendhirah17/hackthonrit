package in.gov.gramdrishti.dto;

import in.gov.gramdrishti.entity.RoleEnum;
import in.gov.gramdrishti.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

public class UserDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserCreateRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        private String mobile;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        @NotNull(message = "Role is required")
        private RoleEnum role;

        private String department;
        private String jurisdictionState;
        private String jurisdictionDistrict;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserUpdateRequest {
        private String name;
        private String mobile;
        private RoleEnum role;
        private UserStatus status;
        private String department;
        private String jurisdictionState;
        private String jurisdictionDistrict;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponseDto {
        private Long id;
        private String name;
        private String email;
        private String mobile;
        private RoleEnum role;
        private UserStatus status;
        private String department;
        private String jurisdictionState;
        private String jurisdictionDistrict;
        private LocalDateTime lastLogin;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
