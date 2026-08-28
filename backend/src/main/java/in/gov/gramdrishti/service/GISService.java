package in.gov.gramdrishti.service;

import in.gov.gramdrishti.dto.GeoJsonDto;
import in.gov.gramdrishti.entity.Infrastructure;
import in.gov.gramdrishti.entity.Project;
import in.gov.gramdrishti.entity.Village;
import in.gov.gramdrishti.repository.InfrastructureRepository;
import in.gov.gramdrishti.repository.ProjectRepository;
import in.gov.gramdrishti.repository.VillageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GISService {

    private final VillageRepository villageRepository;
    private final InfrastructureRepository infrastructureRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public GeoJsonDto.FeatureCollection getVillagesGeoJson() {
        List<Village> villages = villageRepository.findAll();
        List<GeoJsonDto.Feature> features = new ArrayList<>();

        for (Village v : villages) {
            if (v.isDeleted() || v.getLatitude() == null || v.getLongitude() == null) continue;

            Map<String, Object> props = new HashMap<>();
            props.put("id", v.getId());
            props.put("villageName", v.getVillageName());
            props.put("state", v.getState());
            props.put("district", v.getDistrict());
            props.put("block", v.getBlock());
            props.put("population", v.getPopulation());
            props.put("gapScore", v.getGapScore());
            props.put("adequacyScore", v.getAdequacyScore());
            props.put("priority", v.getPriority().name());
            props.put("roadIndex", v.getRoadConnectivityIndex());
            props.put("healthIndex", v.getHealthAccessIndex());
            props.put("waterIndex", v.getWaterSanitationIndex());
            props.put("educationIndex", v.getEducationAccessIndex());
            props.put("digitalIndex", v.getDigitalConnectivityIndex());
            props.put("powerIndex", v.getPowerReliabilityIndex());

            features.add(GeoJsonDto.Feature.builder()
                    .id("village-" + v.getId())
                    .geometry(GeoJsonDto.Geometry.builder()
                            .type("Point")
                            .coordinates(Arrays.asList(v.getLongitude(), v.getLatitude()))
                            .build())
                    .properties(props)
                    .build());
        }

        return GeoJsonDto.FeatureCollection.builder()
                .features(features)
                .build();
    }

    @Transactional(readOnly = true)
    public GeoJsonDto.FeatureCollection getInfrastructureGeoJson() {
        List<Infrastructure> infraList = infrastructureRepository.findAll();
        List<GeoJsonDto.Feature> features = new ArrayList<>();

        for (Infrastructure inf : infraList) {
            if (inf.isDeleted() || inf.getLatitude() == null || inf.getLongitude() == null) continue;

            Map<String, Object> props = new HashMap<>();
            props.put("id", inf.getId());
            props.put("name", inf.getName());
            props.put("infraType", inf.getInfraType().name());
            props.put("status", inf.getStatus().name());
            props.put("conditionScore", inf.getConditionScore());
            props.put("villageName", inf.getVillage() != null ? inf.getVillage().getVillageName() : "");
            props.put("district", inf.getVillage() != null ? inf.getVillage().getDistrict() : "");
            props.put("schemeName", inf.getSchemeName());
            props.put("capacityOrLength", inf.getCapacityOrLength());

            features.add(GeoJsonDto.Feature.builder()
                    .id("infra-" + inf.getId())
                    .geometry(GeoJsonDto.Geometry.builder()
                            .type("Point")
                            .coordinates(Arrays.asList(inf.getLongitude(), inf.getLatitude()))
                            .build())
                    .properties(props)
                    .build());
        }

        return GeoJsonDto.FeatureCollection.builder()
                .features(features)
                .build();
    }

    @Transactional(readOnly = true)
    public GeoJsonDto.FeatureCollection getGapZonesGeoJson() {
        List<Village> criticalVillages = villageRepository.findAll();
        List<GeoJsonDto.Feature> features = new ArrayList<>();

        for (Village v : criticalVillages) {
            if (v.isDeleted() || v.getLatitude() == null || v.getLongitude() == null || v.getGapScore() < 60.0) continue;

            double lat = v.getLatitude();
            double lng = v.getLongitude();
            double delta = 0.025; // ~2.5km radius bounding buffer

            List<List<Double>> ring = Arrays.asList(
                    Arrays.asList(lng - delta, lat - delta),
                    Arrays.asList(lng + delta, lat - delta),
                    Arrays.asList(lng + delta, lat + delta),
                    Arrays.asList(lng - delta, lat + delta),
                    Arrays.asList(lng - delta, lat - delta)
            );

            Map<String, Object> props = new HashMap<>();
            props.put("zoneId", "zone-" + v.getId());
            props.put("centerVillage", v.getVillageName());
            props.put("district", v.getDistrict());
            props.put("state", v.getState());
            props.put("gapScore", v.getGapScore());
            props.put("severity", v.getGapScore() >= 75.0 ? "CRITICAL_DEFICIT" : "HIGH_DEFICIT");
            props.put("priority", v.getPriority().name());

            features.add(GeoJsonDto.Feature.builder()
                    .id("gapzone-" + v.getId())
                    .geometry(GeoJsonDto.Geometry.builder()
                            .type("Polygon")
                            .coordinates(Collections.singletonList(ring))
                            .build())
                    .properties(props)
                    .build());
        }

        return GeoJsonDto.FeatureCollection.builder()
                .features(features)
                .build();
    }

    @Transactional(readOnly = true)
    public GeoJsonDto.FeatureCollection getProjectBoundariesGeoJson() {
        List<Project> projects = projectRepository.findAll();
        List<GeoJsonDto.Feature> features = new ArrayList<>();

        for (Project p : projects) {
            if (p.isDeleted() || p.getLatitude() == null || p.getLongitude() == null) continue;

            double lat = p.getLatitude();
            double lng = p.getLongitude();
            double delta = 0.035; // ~3.5km catchment polygon

            List<List<Double>> ring = Arrays.asList(
                    Arrays.asList(lng - delta, lat - delta),
                    Arrays.asList(lng + delta, lat - delta),
                    Arrays.asList(lng + (delta * 1.2), lat + (delta * 0.8)),
                    Arrays.asList(lng - delta, lat + delta),
                    Arrays.asList(lng - delta, lat - delta)
            );

            Map<String, Object> props = new HashMap<>();
            props.put("projectId", p.getId());
            props.put("projectCode", p.getProjectCode());
            props.put("projectName", p.getProjectName());
            props.put("status", p.getStatus().name());
            props.put("priority", p.getPriority().name());
            props.put("budgetAllocated", p.getBudgetAllocated());
            props.put("baselineGapScore", p.getBaselineGapScore());
            props.put("currentGapScore", p.getCurrentGapScore());
            props.put("gapReductionPct", p.getGapReductionPct());
            props.put("villageName", p.getVillageName());

            features.add(GeoJsonDto.Feature.builder()
                    .id("proj-boundary-" + p.getId())
                    .geometry(GeoJsonDto.Geometry.builder()
                            .type("Polygon")
                            .coordinates(Collections.singletonList(ring))
                            .build())
                    .properties(props)
                    .build());
        }

        return GeoJsonDto.FeatureCollection.builder()
                .features(features)
                .build();
    }
}
