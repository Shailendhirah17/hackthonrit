package in.gov.gramdrishti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeoJsonDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureCollection {
        @Builder.Default
        private String type = "FeatureCollection";
        @Builder.Default
        private List<Feature> features = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Feature {
        @Builder.Default
        private String type = "Feature";
        private String id;
        private Geometry geometry;
        private Map<String, Object> properties;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Geometry {
        private String type; // "Point", "Polygon", "LineString", "MultiPolygon"
        private Object coordinates; // [lng, lat] for Point or [[[lng, lat], ...]] for Polygon
    }
}
