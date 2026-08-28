package in.gov.gramdrishti.controller;

import in.gov.gramdrishti.dto.ApiResponse;
import in.gov.gramdrishti.dto.VillageDto;
import in.gov.gramdrishti.entity.PriorityLevel;
import in.gov.gramdrishti.service.VillageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/villages")
@RequiredArgsConstructor
@Tag(name = "Villages & Demographics", description = "Query Indian rural habitations, census demographics, and gap metrics")
public class VillageController {

    private final VillageService villageService;

    @GetMapping
    @Operation(summary = "Filter and search villages with multi-factor criteria")
    public ResponseEntity<ApiResponse<Page<VillageDto.VillageResponseDto>>> getVillages(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) PriorityLevel priority,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "gapScore") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<VillageDto.VillageResponseDto> list = villageService.getVillages(state, district, priority, query, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/all")
    @Operation(summary = "Get compact list of all non-deleted villages")
    public ResponseEntity<ApiResponse<List<VillageDto.VillageResponseDto>>> getAllVillages() {
        return ResponseEntity.ok(ApiResponse.ok(villageService.getAllVillagesList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get village details by ID")
    public ResponseEntity<ApiResponse<VillageDto.VillageResponseDto>> getVillageById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(villageService.getVillageById(id)));
    }
}
