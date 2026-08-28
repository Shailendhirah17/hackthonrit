package in.gov.gramdrishti.controller;

import in.gov.gramdrishti.dto.ApiResponse;
import in.gov.gramdrishti.dto.InfrastructureDto;
import in.gov.gramdrishti.entity.InfraStatus;
import in.gov.gramdrishti.entity.InfraType;
import in.gov.gramdrishti.security.UserPrincipal;
import in.gov.gramdrishti.service.InfrastructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/infrastructure")
@RequiredArgsConstructor
@Tag(name = "Infrastructure Assets", description = "Query and record rural infrastructure assets (roads, health centres, schools, water, power)")
public class InfrastructureController {

    private final InfrastructureService infrastructureService;

    @GetMapping
    @Operation(summary = "Query infrastructure assets by type, status, or village")
    public ResponseEntity<ApiResponse<List<InfrastructureDto.InfrastructureResponseDto>>> getInfrastructure(
            @RequestParam(required = false) InfraType infraType,
            @RequestParam(required = false) InfraStatus status,
            @RequestParam(required = false) Long villageId
    ) {
        List<InfrastructureDto.InfrastructureResponseDto> list = infrastructureService.getInfrastructureList(infraType, status, villageId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'FIELD_OFFICER')")
    @Operation(summary = "Record new field infrastructure asset (Super Admin, Admin, PM, Field Officer)")
    public ResponseEntity<ApiResponse<InfrastructureDto.InfrastructureResponseDto>> createInfrastructure(
            @Valid @RequestBody InfrastructureDto.InfrastructureCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        InfrastructureDto.InfrastructureResponseDto created = infrastructureService.createInfrastructure(request, principal != null ? principal.getEmail() : "SYSTEM");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Infrastructure logged successfully", created));
    }
}
