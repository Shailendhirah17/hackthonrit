package in.gov.gramdrishti.controller;

import in.gov.gramdrishti.dto.ApiResponse;
import in.gov.gramdrishti.dto.AuthDto;
import in.gov.gramdrishti.dto.UserDto;
import in.gov.gramdrishti.security.UserPrincipal;
import in.gov.gramdrishti.service.AuthService;
import in.gov.gramdrishti.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & Authorization", description = "JWT Login, HttpOnly Refresh Token, and Profile Management")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Value("${app.jwt.cookie-name:gramdrishti_refresh_token}")
    private String cookieName;

    @PostMapping("/login")
    @Operation(summary = "User login with email and password")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthService.AuthResult result = authService.login(request, ip, userAgent);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.getRefreshCookie().toString())
                .body(ApiResponse.ok("Login successful", result.getAuthResponse()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT access token using HttpOnly cookie or body token")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> refreshToken(
            @RequestBody(required = false) AuthDto.RefreshTokenRequest bodyRequest,
            HttpServletRequest httpRequest
    ) {
        String refreshToken = null;
        if (httpRequest.getCookies() != null) {
            for (Cookie c : httpRequest.getCookies()) {
                if (cookieName.equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null && bodyRequest != null) {
            refreshToken = bodyRequest.getRefreshToken();
        }

        String ip = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthService.AuthResult result = authService.refreshToken(refreshToken, ip, userAgent);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.getRefreshCookie().toString())
                .body(ApiResponse.ok("Token refreshed", result.getAuthResponse()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and invalidate refresh cookie")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authService.createLogoutCookie().toString())
                .body(ApiResponse.ok("Logged out successfully", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserDto.UserResponseDto>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("UNAUTHORIZED", "Not logged in"));
        }
        UserDto.UserResponseDto userDto = userService.getUserById(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(userDto));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AuthDto.ChangePasswordRequest request
    ) {
        authService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password updated successfully", null));
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
