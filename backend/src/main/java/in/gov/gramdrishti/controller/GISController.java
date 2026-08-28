package in.gov.gramdrishti.controller;

import in.gov.gramdrishti.dto.GeoJsonDto;
import in.gov.gramdrishti.service.GISService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gis")
@RequiredArgsConstructor
@Tag(name = "GIS & Spatial Map Layers", description = "GeoJSON endpoints for interactive map rendering")
public class GISController {

    private final GISService gisService;

    @GetMapping("/villages")
    @Operation(summary = "Get GeoJSON FeatureCollection of all villages with gap scores and indicators")
    public ResponseEntity<GeoJsonDto.FeatureCollection> getVillagesGeoJson() {
        return ResponseEntity.ok(gisService.getVillagesGeoJson());
    }

    @GetMapping("/infrastructure")
    @Operation(summary = "Get GeoJSON FeatureCollection of all infrastructure assets")
    public ResponseEntity<GeoJsonDto.FeatureCollection> getInfrastructureGeoJson() {
        return ResponseEntity.ok(gisService.getInfrastructureGeoJson());
    }

    @GetMapping("/gap-zones")
    @Operation(summary = "Get GeoJSON FeatureCollection of high-deficit gap zones and heat buffers")
    public ResponseEntity<GeoJsonDto.FeatureCollection> getGapZonesGeoJson() {
        return ResponseEntity.ok(gisService.getGapZonesGeoJson());
    }

    @GetMapping("/project-boundary")
    @Operation(summary = "Get GeoJSON FeatureCollection of project boundary polygons and catchment areas")
    public ResponseEntity<GeoJsonDto.FeatureCollection> getProjectBoundariesGeoJson() {
        return ResponseEntity.ok(gisService.getProjectBoundariesGeoJson());
    }
}
