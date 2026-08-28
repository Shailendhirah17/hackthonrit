package in.gov.gramdrishti.service;

import in.gov.gramdrishti.dto.VillageDto;
import in.gov.gramdrishti.entity.PriorityLevel;
import in.gov.gramdrishti.entity.Village;
import in.gov.gramdrishti.exception.ResourceNotFoundException;
import in.gov.gramdrishti.repository.InfrastructureRepository;
import in.gov.gramdrishti.repository.ProjectRepository;
import in.gov.gramdrishti.repository.VillageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VillageService {

    private final VillageRepository villageRepository;
    private final InfrastructureRepository infrastructureRepository;
    private final ProjectRepository projectRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<VillageDto.VillageResponseDto> getVillages(String state, String district, PriorityLevel priority, String query, Pageable pageable) {
        return villageRepository.filterVillages(state, district, priority, query, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<VillageDto.VillageResponseDto> getAllVillagesList() {
        return villageRepository.findAll().stream()
                .filter(v -> !v.isDeleted())
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VillageDto.VillageResponseDto getVillageById(Long id) {
        Village village = villageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Village not found with id: " + id));
        return mapToDto(village);
    }

    @Transactional
    public Village updateVillageGapMetrics(Long id, Double gapScore, PriorityLevel priority, String performedByEmail) {
        Village village = villageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Village not found with id: " + id));

        village.setGapScore(gapScore);
        village.setAdequacyScore(100.0 - gapScore);
        village.setPriority(priority);

        Village updated = villageRepository.save(village);
        auditLogService.logAction(null, performedByEmail, "SYSTEM", "VILLAGE_GAP_UPDATED", "VILLAGE", id.toString(), null, "Updated gap score to " + gapScore, null, null);
        return updated;
    }

    public VillageDto.VillageResponseDto mapToDto(Village village) {
        int infraCount = infrastructureRepository.findByVillageId(village.getId()).size();
        int projCount = projectRepository.findByVillageId(village.getId()).size();

        return VillageDto.VillageResponseDto.builder()
                .id(village.getId())
                .censusCode(village.getCensusCode())
                .villageName(village.getVillageName())
                .state(village.getState())
                .district(village.getDistrict())
                .block(village.getBlock())
                .gramPanchayat(village.getGramPanchayat())
                .latitude(village.getLatitude())
                .longitude(village.getLongitude())
                .population(village.getPopulation())
                .householdCount(village.getHouseholdCount())
                .gapScore(village.getGapScore())
                .adequacyScore(village.getAdequacyScore())
                .priority(village.getPriority())
                .roadConnectivityIndex(village.getRoadConnectivityIndex())
                .healthAccessIndex(village.getHealthAccessIndex())
                .educationAccessIndex(village.getEducationAccessIndex())
                .waterSanitationIndex(village.getWaterSanitationIndex())
                .digitalConnectivityIndex(village.getDigitalConnectivityIndex())
                .powerReliabilityIndex(village.getPowerReliabilityIndex())
                .infrastructureCount(infraCount)
                .activeProjectsCount(projCount)
                .createdAt(village.getCreatedAt())
                .updatedAt(village.getUpdatedAt())
                .build();
    }
}
