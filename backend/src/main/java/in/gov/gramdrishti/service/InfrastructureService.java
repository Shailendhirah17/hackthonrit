package in.gov.gramdrishti.service;

import in.gov.gramdrishti.dto.InfrastructureDto;
import in.gov.gramdrishti.entity.InfraStatus;
import in.gov.gramdrishti.entity.InfraType;
import in.gov.gramdrishti.entity.Infrastructure;
import in.gov.gramdrishti.entity.Village;
import in.gov.gramdrishti.exception.ResourceNotFoundException;
import in.gov.gramdrishti.repository.InfrastructureRepository;
import in.gov.gramdrishti.repository.VillageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InfrastructureService {

    private final InfrastructureRepository infrastructureRepository;
    private final VillageRepository villageRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<InfrastructureDto.InfrastructureResponseDto> getInfrastructureList(InfraType infraType, InfraStatus status, Long villageId) {
        return infrastructureRepository.filterInfrastructure(infraType, status, villageId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public InfrastructureDto.InfrastructureResponseDto createInfrastructure(InfrastructureDto.InfrastructureCreateRequest request, String performedByEmail) {
        Village village = villageRepository.findById(request.getVillageId())
                .orElseThrow(() -> new ResourceNotFoundException("Village not found with id: " + request.getVillageId()));

        Infrastructure infrastructure = Infrastructure.builder()
                .village(village)
                .infraType(request.getInfraType())
                .name(request.getName())
                .status(request.getStatus())
                .conditionScore(request.getConditionScore())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .capacityOrLength(request.getCapacityOrLength())
                .establishedYear(request.getEstablishedYear())
                .schemeName(request.getSchemeName())
                .attributesJson(request.getAttributesJson())
                .deleted(false)
                .build();

        Infrastructure saved = infrastructureRepository.save(infrastructure);

        auditLogService.logAction(null, performedByEmail, "FIELD_OFFICER", "INFRASTRUCTURE_CREATED", "INFRASTRUCTURE", saved.getId().toString(), null, "Created infrastructure: " + saved.getName(), null, null);

        return mapToDto(saved);
    }

    public InfrastructureDto.InfrastructureResponseDto mapToDto(Infrastructure infra) {
        return InfrastructureDto.InfrastructureResponseDto.builder()
                .id(infra.getId())
                .villageId(infra.getVillage().getId())
                .villageName(infra.getVillage().getVillageName())
                .state(infra.getVillage().getState())
                .district(infra.getVillage().getDistrict())
                .infraType(infra.getInfraType())
                .name(infra.getName())
                .status(infra.getStatus())
                .conditionScore(infra.getConditionScore())
                .latitude(infra.getLatitude())
                .longitude(infra.getLongitude())
                .capacityOrLength(infra.getCapacityOrLength())
                .establishedYear(infra.getEstablishedYear())
                .schemeName(infra.getSchemeName())
                .attributesJson(infra.getAttributesJson())
                .createdAt(infra.getCreatedAt())
                .updatedAt(infra.getUpdatedAt())
                .build();
    }
}
