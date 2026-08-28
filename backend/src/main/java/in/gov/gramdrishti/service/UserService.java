package in.gov.gramdrishti.service;

import in.gov.gramdrishti.dto.UserDto;
import in.gov.gramdrishti.entity.Role;
import in.gov.gramdrishti.entity.User;
import in.gov.gramdrishti.entity.UserStatus;
import in.gov.gramdrishti.exception.BadRequestException;
import in.gov.gramdrishti.exception.ResourceNotFoundException;
import in.gov.gramdrishti.repository.RoleRepository;
import in.gov.gramdrishti.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<UserDto.UserResponseDto> getUsers(String query, Pageable pageable) {
        return userRepository.searchUsers(query, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public UserDto.UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDto(user);
    }

    @Transactional
    public UserDto.UserResponseDto createUser(UserDto.UserCreateRequest request, String performedByEmail) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new BadRequestException("Invalid role specified: " + request.getRole()));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status(UserStatus.ACTIVE)
                .department(request.getDepartment())
                .jurisdictionState(request.getJurisdictionState())
                .jurisdictionDistrict(request.getJurisdictionDistrict())
                .deleted(false)
                .build();

        User savedUser = userRepository.save(user);

        auditLogService.logAction(savedUser.getId(), performedByEmail, "ADMIN", "USER_CREATED", "USER", savedUser.getId().toString(), null, "Created user: " + savedUser.getEmail(), null, null);

        return mapToDto(savedUser);
    }

    @Transactional
    public UserDto.UserResponseDto updateUser(Long id, UserDto.UserUpdateRequest request, String performedByEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getMobile() != null) user.setMobile(request.getMobile());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getJurisdictionState() != null) user.setJurisdictionState(request.getJurisdictionState());
        if (request.getJurisdictionDistrict() != null) user.setJurisdictionDistrict(request.getJurisdictionDistrict());
        if (request.getStatus() != null) user.setStatus(request.getStatus());

        if (request.getRole() != null && request.getRole() != user.getRole().getName()) {
            Role role = roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new BadRequestException("Invalid role: " + request.getRole()));
            user.setRole(role);
        }

        User updatedUser = userRepository.save(user);

        auditLogService.logAction(updatedUser.getId(), performedByEmail, "ADMIN", "USER_UPDATED", "USER", updatedUser.getId().toString(), null, "Updated user: " + updatedUser.getEmail(), null, null);

        return mapToDto(updatedUser);
    }

    @Transactional
    public UserDto.UserResponseDto updateUserStatus(Long id, UserStatus status, String performedByEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setStatus(status);
        User updated = userRepository.save(user);

        auditLogService.logAction(user.getId(), performedByEmail, "ADMIN", "USER_STATUS_CHANGED", "USER", user.getId().toString(), null, "Status changed to: " + status, null, null);

        return mapToDto(updated);
    }

    @Transactional
    public void deleteUser(Long id, String performedByEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(performedByEmail);
        userRepository.save(user);

        auditLogService.logAction(user.getId(), performedByEmail, "ADMIN", "USER_DELETED", "USER", user.getId().toString(), null, "Soft deleted user: " + user.getEmail(), null, null);
    }

    @Transactional
    public void restoreUser(Long id, String performedByEmail) {
        userRepository.restoreDeletedUser(id);
        auditLogService.logAction(id, performedByEmail, "SUPER_ADMIN", "USER_RESTORED", "USER", id.toString(), null, "Restored deleted user", null, null);
    }

    public UserDto.UserResponseDto mapToDto(User user) {
        return UserDto.UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole().getName())
                .status(user.getStatus())
                .department(user.getDepartment())
                .jurisdictionState(user.getJurisdictionState())
                .jurisdictionDistrict(user.getJurisdictionDistrict())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
