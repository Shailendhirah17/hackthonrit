package in.gov.gramdrishti.service;

import in.gov.gramdrishti.dto.AuthDto;
import in.gov.gramdrishti.entity.User;
import in.gov.gramdrishti.entity.UserStatus;
import in.gov.gramdrishti.exception.AccountLockedException;
import in.gov.gramdrishti.exception.BadRequestException;
import in.gov.gramdrishti.exception.ResourceNotFoundException;
import in.gov.gramdrishti.exception.UnauthorizedException;
import in.gov.gramdrishti.repository.UserRepository;
import in.gov.gramdrishti.security.AccountLockoutService;
import in.gov.gramdrishti.security.JwtTokenProvider;
import in.gov.gramdrishti.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AccountLockoutService accountLockoutService;
    private final AuditLogService auditLogService;

    @Value("${app.jwt.cookie-name:gramdrishti_refresh_token}")
    private String cookieName;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationInMs;

    @Transactional
    public AuthResult login(AuthDto.LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    auditLogService.logAction(null, request.getEmail(), "UNKNOWN", "LOGIN_FAILED", "AUTH", null, null, "User not found", ipAddress, userAgent);
                    return new BadCredentialsException("Invalid email or password");
                });

        if (user.isDeleted() || user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Your account is " + user.getStatus() + ". Please contact administrator.");
        }

        if (accountLockoutService.isAccountLocked(user)) {
            auditLogService.logAction(user.getId(), user.getEmail(), user.getRole().getName().name(), "LOGIN_BLOCKED_LOCKOUT", "AUTH", user.getId().toString(), null, "Account locked", ipAddress, userAgent);
            throw new AccountLockedException("Account temporarily locked due to multiple failed login attempts. Please try again in 15 minutes.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            accountLockoutService.resetFailedAttempts(user.getEmail());

            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

            Set<String> permissions = (user.getRole() != null && user.getRole().getPermissions() != null)
                    ? user.getRole().getPermissions().stream()
                            .filter(java.util.Objects::nonNull)
                            .map(p -> p.getName())
                            .filter(java.util.Objects::nonNull)
                            .collect(Collectors.toSet())
                    : java.util.Collections.emptySet();

            AuthDto.AuthResponse authResponse = AuthDto.AuthResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(3600)
                    .userId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole().getName())
                    .department(user.getDepartment())
                    .permissions(permissions)
                    .build();

            auditLogService.logAction(user.getId(), user.getEmail(), user.getRole().getName().name(), "LOGIN_SUCCESS", "AUTH", user.getId().toString(), null, "Successful login", ipAddress, userAgent);

            ResponseCookie refreshCookie = ResponseCookie.from(cookieName, refreshToken)
                    .httpOnly(true)
                    .secure(false) // Set to true in HTTPS production
                    .path("/")
                    .maxAge(refreshExpirationInMs / 1000)
                    .sameSite("Lax")
                    .build();

            return new AuthResult(authResponse, refreshCookie);
        } catch (BadCredentialsException ex) {
            accountLockoutService.recordFailedLogin(user.getEmail());
            auditLogService.logAction(user.getId(), user.getEmail(), user.getRole().getName().name(), "LOGIN_FAILED", "AUTH", user.getId().toString(), null, "Incorrect password", ipAddress, userAgent);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Transactional(readOnly = true)
    public AuthResult refreshToken(String refreshToken, String ipAddress, String userAgent) {
        if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isDeleted() || user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is inactive or deleted");
        }

        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(email);

        Set<String> permissions = (user.getRole() != null && user.getRole().getPermissions() != null)
                ? user.getRole().getPermissions().stream()
                        .filter(java.util.Objects::nonNull)
                        .map(p -> p.getName())
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet())
                : java.util.Collections.emptySet();

        AuthDto.AuthResponse authResponse = AuthDto.AuthResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(3600)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .department(user.getDepartment())
                .permissions(permissions)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(cookieName, newRefreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshExpirationInMs / 1000)
                .sameSite("Lax")
                .build();

        return new AuthResult(authResponse, refreshCookie);
    }

    public ResponseCookie createLogoutCookie() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

    @Transactional
    public void changePassword(Long userId, AuthDto.ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        auditLogService.logAction(user.getId(), user.getEmail(), user.getRole().getName().name(), "PASSWORD_CHANGED", "USER", user.getId().toString(), null, "Password changed successfully", null, null);
    }

    public static class AuthResult {
        private final AuthDto.AuthResponse authResponse;
        private final ResponseCookie refreshCookie;

        public AuthResult(AuthDto.AuthResponse authResponse, ResponseCookie refreshCookie) {
            this.authResponse = authResponse;
            this.refreshCookie = refreshCookie;
        }

        public AuthDto.AuthResponse getAuthResponse() {
            return authResponse;
        }

        public ResponseCookie getRefreshCookie() {
            return refreshCookie;
        }
    }
}
