package in.gov.gramdrishti.service;

import in.gov.gramdrishti.entity.*;
import in.gov.gramdrishti.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeederService implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final VillageRepository villageRepository;
    private final InfrastructureRepository infrastructureRepository;
    private final ProjectRepository projectRepository;
    private final RequirementRepository requirementRepository;
    private final FolderRepository folderRepository;
    private final AssetRepository assetRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0 && userRepository.count() > 0) {
            log.info("Database already seeded with roles and initial users. Skipping seeder.");
            return;
        }

        log.info("Starting GramDrishti AI Database Seeder...");

        // 1. Permissions
        Map<String, Permission> permMap = seedPermissions();

        // 2. Roles with Permission Matrix
        Map<RoleEnum, Role> roleMap = seedRoles(permMap);

        // 3. Initial Users (All 6 Roles)
        seedUsers(roleMap);

        // 4. Indian Rural Villages
        List<Village> villages = seedVillages();

        // 5. Infrastructure
        seedInfrastructure(villages);

        // 6. Projects (including closed-loop completed impact project)
        seedProjectsAndRequirements(villages);

        log.info("GramDrishti AI Database successfully seeded with production demo dataset!");
    }

    private Map<String, Permission> seedPermissions() {
        String[][] perms = {
                {"USER_MANAGEMENT", "ADMIN", "Create, edit, view and manage system user accounts"},
                {"ROLE_MANAGEMENT", "ADMIN", "Define and configure roles and permissions"},
                {"PROJECT_CREATE_EDIT", "PROJECT", "Create, update, and manage rural infrastructure projects"},
                {"PROJECT_VIEW", "PROJECT", "View project details and milestones"},
                {"REQUIREMENT_CREATE", "REQUIREMENT", "Identify, tag, and approve infrastructure gap requirements"},
                {"FIELD_DATA_ENTRY", "FIELD", "Submit geotagged field survey data and evidence assets"},
                {"AI_ANALYSIS", "AI", "Execute computer vision scans and calculate composite gap scores"},
                {"GIS_ANALYSIS", "GIS", "Access and query spatial GIS layers and deficit heat zones"},
                {"REPORTS", "REPORTS", "Generate and export analytical and impact intelligence reports"},
                {"FILE_UPLOAD", "DAM", "Upload digital evidence, drone orthophotos, and satellite tiles"},
                {"FILE_DELETE", "DAM", "Delete or archive managed project digital assets"},
                {"AUDIT_LOGS", "AUDIT", "Inspect system-wide security, auth, and state-change audit trail"},
                {"SYSTEM_SETTINGS", "SYSTEM", "Configure AI scoring model weights and platform integrations"}
        };

        Map<String, Permission> map = new HashMap<>();
        for (String[] p : perms) {
            Permission permission = permissionRepository.findByName(p[0])
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .name(p[0])
                            .category(p[1])
                            .description(p[2])
                            .build()));
            map.put(p[0], permission);
        }
        return map;
    }

    private Map<RoleEnum, Role> seedRoles(Map<String, Permission> p) {
        Map<RoleEnum, Role> roleMap = new HashMap<>();

        // SUPER_ADMIN (All 13 Permissions)
        Set<Permission> superAdminPerms = new HashSet<>(p.values());
        roleMap.put(RoleEnum.SUPER_ADMIN, createRoleIfAbsent(RoleEnum.SUPER_ADMIN, "Super Administrator with apex system governance", superAdminPerms));

        // ADMIN
        Set<Permission> adminPerms = new HashSet<>(Arrays.asList(
                p.get("USER_MANAGEMENT"), p.get("PROJECT_CREATE_EDIT"), p.get("PROJECT_VIEW"),
                p.get("REQUIREMENT_CREATE"), p.get("FIELD_DATA_ENTRY"), p.get("AI_ANALYSIS"),
                p.get("GIS_ANALYSIS"), p.get("REPORTS"), p.get("FILE_UPLOAD"),
                p.get("FILE_DELETE"), p.get("AUDIT_LOGS"), p.get("SYSTEM_SETTINGS")
        ));
        roleMap.put(RoleEnum.ADMIN, createRoleIfAbsent(RoleEnum.ADMIN, "State & District Level Administrator", adminPerms));

        // PROJECT_MANAGER
        Set<Permission> pmPerms = new HashSet<>(Arrays.asList(
                p.get("PROJECT_CREATE_EDIT"), p.get("PROJECT_VIEW"), p.get("REQUIREMENT_CREATE"),
                p.get("FIELD_DATA_ENTRY"), p.get("AI_ANALYSIS"), p.get("GIS_ANALYSIS"),
                p.get("REPORTS"), p.get("FILE_UPLOAD"), p.get("FILE_DELETE"), p.get("AUDIT_LOGS")
        ));
        roleMap.put(RoleEnum.PROJECT_MANAGER, createRoleIfAbsent(RoleEnum.PROJECT_MANAGER, "Project Manager overseeing rural infrastructure execution", pmPerms));

        // ANALYST
        Set<Permission> analystPerms = new HashSet<>(Arrays.asList(
                p.get("PROJECT_VIEW"), p.get("REQUIREMENT_CREATE"), p.get("AI_ANALYSIS"),
                p.get("GIS_ANALYSIS"), p.get("REPORTS"), p.get("FILE_UPLOAD")
        ));
        roleMap.put(RoleEnum.ANALYST, createRoleIfAbsent(RoleEnum.ANALYST, "Rural Intelligence and GIS Data Analyst", analystPerms));

        // FIELD_OFFICER
        Set<Permission> fieldOfficerPerms = new HashSet<>(Arrays.asList(
                p.get("PROJECT_VIEW"), p.get("FIELD_DATA_ENTRY"), p.get("REPORTS"), p.get("FILE_UPLOAD")
        ));
        roleMap.put(RoleEnum.FIELD_OFFICER, createRoleIfAbsent(RoleEnum.FIELD_OFFICER, "Block / Gram Panchayat Field Officer conducting ground verification", fieldOfficerPerms));

        // VIEWER
        Set<Permission> viewerPerms = new HashSet<>(Arrays.asList(
                p.get("PROJECT_VIEW"), p.get("GIS_ANALYSIS"), p.get("REPORTS")
        ));
        roleMap.put(RoleEnum.VIEWER, createRoleIfAbsent(RoleEnum.VIEWER, "Public / Departmental Read-only Stakeholder", viewerPerms));

        return roleMap;
    }

    private Role createRoleIfAbsent(RoleEnum roleEnum, String desc, Set<Permission> perms) {
        return roleRepository.findByName(roleEnum)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(roleEnum)
                        .description(desc)
                        .permissions(perms)
                        .build()));
    }

    private void seedUsers(Map<RoleEnum, Role> roleMap) {
        String defaultPasswordHash = passwordEncoder.encode("Password@123");

        Object[][] users = {
                {"Dr. Rajesh Verma", "superadmin@gramdrishti.gov.in", "9876500001", RoleEnum.SUPER_ADMIN, "Ministry of Rural Development (MoRD)", "National", "All"},
                {"Pooja Sharma (IAS)", "admin@gramdrishti.gov.in", "9876500002", RoleEnum.ADMIN, "State Planning Board", "Maharashtra", "Gadchiroli"},
                {"Vikramaditya Rao", "pm@gramdrishti.gov.in", "9876500003", RoleEnum.PROJECT_MANAGER, "PMGSY Infrastructure Directorate", "Odisha", "Kalahandi"},
                {"Ananya Sengupta", "analyst@gramdrishti.gov.in", "9876500004", RoleEnum.ANALYST, "GIS & Remote Sensing Cell", "Rajasthan", "Barmer"},
                {"Suresh Naik", "field@gramdrishti.gov.in", "9876500005", RoleEnum.FIELD_OFFICER, "Panchayat Development Office", "Maharashtra", "Gadchiroli"},
                {"Kavita Nair", "viewer@gramdrishti.gov.in", "9876500006", RoleEnum.VIEWER, "Citizen Oversight & Media Portal", "National", "All"},
                {"Hackathon Demo Judge", "demo.viewer@gramdrishti.gov.in", "9876500007", RoleEnum.VIEWER, "Evaluation & Jury Directorate", "National", "All"}
        };

        for (Object[] u : users) {
            String email = (String) u[1];
            if (!userRepository.existsByEmail(email)) {
                User user = User.builder()
                        .name((String) u[0])
                        .email(email)
                        .mobile((String) u[2])
                        .passwordHash(defaultPasswordHash)
                        .role(roleMap.get((RoleEnum) u[3]))
                        .status(UserStatus.ACTIVE)
                        .department((String) u[4])
                        .jurisdictionState((String) u[5])
                        .jurisdictionDistrict((String) u[6])
                        .deleted(false)
                        .build();
                userRepository.save(user);
            }
        }
    }

    private List<Village> seedVillages() {
        if (villageRepository.count() > 0) return villageRepository.findAll();

        List<Village> list = Arrays.asList(
                // Maharashtra - Gadchiroli
                Village.builder()
                        .censusCode("CEN-MH-538910")
                        .villageName("Bhamragad")
                        .state("Maharashtra")
                        .district("Gadchiroli")
                        .block("Bhamragad")
                        .gramPanchayat("Bhamragad Gram Panchayat")
                        .latitude(19.6433)
                        .longitude(80.3524)
                        .population(2450)
                        .householdCount(480)
                        .gapScore(86.4)
                        .adequacyScore(13.6)
                        .priority(PriorityLevel.CRITICAL)
                        .roadConnectivityIndex(24.0)
                        .healthAccessIndex(18.0)
                        .educationAccessIndex(42.0)
                        .waterSanitationIndex(31.0)
                        .digitalConnectivityIndex(15.0)
                        .powerReliabilityIndex(48.0)
                        .build(),

                Village.builder()
                        .censusCode("CEN-MH-538911")
                        .villageName("Etapalli")
                        .state("Maharashtra")
                        .district("Gadchiroli")
                        .block("Etapalli")
                        .gramPanchayat("Etapalli GP")
                        .latitude(19.7891)
                        .longitude(80.2014)
                        .population(3120)
                        .householdCount(620)
                        .gapScore(78.2)
                        .adequacyScore(21.8)
                        .priority(PriorityLevel.CRITICAL)
                        .roadConnectivityIndex(32.0)
                        .healthAccessIndex(28.0)
                        .educationAccessIndex(55.0)
                        .waterSanitationIndex(45.0)
                        .digitalConnectivityIndex(30.0)
                        .powerReliabilityIndex(60.0)
                        .build(),

                // Odisha - Kalahandi
                Village.builder()
                        .censusCode("CEN-OD-421098")
                        .villageName("Thuamul Rampur")
                        .state("Odisha")
                        .district("Kalahandi")
                        .block("Thuamul Rampur")
                        .gramPanchayat("Rampur GP")
                        .latitude(19.5342)
                        .longitude(82.9761)
                        .population(1890)
                        .householdCount(390)
                        .gapScore(88.0)
                        .adequacyScore(12.0)
                        .priority(PriorityLevel.CRITICAL)
                        .roadConnectivityIndex(15.0)
                        .healthAccessIndex(12.0)
                        .educationAccessIndex(38.0)
                        .waterSanitationIndex(28.0)
                        .digitalConnectivityIndex(10.0)
                        .powerReliabilityIndex(35.0)
                        .build(),

                Village.builder()
                        .censusCode("CEN-OD-421099")
                        .villageName("Lanjigarh")
                        .state("Odisha")
                        .district("Kalahandi")
                        .block("Lanjigarh")
                        .gramPanchayat("Lanjigarh GP")
                        .latitude(19.7188)
                        .longitude(83.3541)
                        .population(4200)
                        .householdCount(890)
                        .gapScore(62.5)
                        .adequacyScore(37.5)
                        .priority(PriorityLevel.HIGH)
                        .roadConnectivityIndex(52.0)
                        .healthAccessIndex(48.0)
                        .educationAccessIndex(65.0)
                        .waterSanitationIndex(58.0)
                        .digitalConnectivityIndex(40.0)
                        .powerReliabilityIndex(70.0)
                        .build(),

                // Rajasthan - Barmer
                Village.builder()
                        .censusCode("CEN-RJ-219482")
                        .villageName("Chohtan")
                        .state("Rajasthan")
                        .district("Barmer")
                        .block("Chohtan")
                        .gramPanchayat("Chohtan GP")
                        .latitude(25.5678)
                        .longitude(71.0543)
                        .population(3800)
                        .householdCount(710)
                        .gapScore(74.0)
                        .adequacyScore(26.0)
                        .priority(PriorityLevel.HIGH)
                        .roadConnectivityIndex(45.0)
                        .healthAccessIndex(35.0)
                        .educationAccessIndex(50.0)
                        .waterSanitationIndex(18.0) // Desert acute water shortage
                        .digitalConnectivityIndex(55.0)
                        .powerReliabilityIndex(65.0)
                        .build(),

                // Chhattisgarh - Bastar
                Village.builder()
                        .censusCode("CEN-CG-310842")
                        .villageName("Darbha")
                        .state("Chhattisgarh")
                        .district("Bastar")
                        .block("Darbha")
                        .gramPanchayat("Darbha GP")
                        .latitude(18.8762)
                        .longitude(81.8904)
                        .population(2100)
                        .householdCount(430)
                        .gapScore(82.5)
                        .adequacyScore(17.5)
                        .priority(PriorityLevel.CRITICAL)
                        .roadConnectivityIndex(28.0)
                        .healthAccessIndex(20.0)
                        .educationAccessIndex(44.0)
                        .waterSanitationIndex(38.0)
                        .digitalConnectivityIndex(12.0)
                        .powerReliabilityIndex(40.0)
                        .build(),

                // Karnataka - Raichur
                Village.builder()
                        .censusCode("CEN-KA-610294")
                        .villageName("Deodurga")
                        .state("Karnataka")
                        .district("Raichur")
                        .block("Deodurga")
                        .gramPanchayat("Deodurga GP")
                        .latitude(16.4251)
                        .longitude(76.9382)
                        .population(5100)
                        .householdCount(1050)
                        .gapScore(48.0)
                        .adequacyScore(52.0)
                        .priority(PriorityLevel.MEDIUM)
                        .roadConnectivityIndex(68.0)
                        .healthAccessIndex(62.0)
                        .educationAccessIndex(70.0)
                        .waterSanitationIndex(54.0)
                        .digitalConnectivityIndex(60.0)
                        .powerReliabilityIndex(80.0)
                        .build(),

                // Kerala - Wayanad
                Village.builder()
                        .censusCode("CEN-KL-781920")
                        .villageName("Meppadi")
                        .state("Kerala")
                        .district("Wayanad")
                        .block("Vythiri")
                        .gramPanchayat("Meppadi GP")
                        .latitude(11.5542)
                        .longitude(76.1264)
                        .population(4800)
                        .householdCount(1100)
                        .gapScore(34.2)
                        .adequacyScore(65.8)
                        .priority(PriorityLevel.LOW)
                        .roadConnectivityIndex(82.0)
                        .healthAccessIndex(78.0)
                        .educationAccessIndex(90.0)
                        .waterSanitationIndex(75.0)
                        .digitalConnectivityIndex(85.0)
                        .powerReliabilityIndex(88.0)
                        .build()
        );

        return villageRepository.saveAll(list);
    }

    private void seedInfrastructure(List<Village> villages) {
        if (infrastructureRepository.count() > 0) return;

        Village bhamragad = villages.get(0);
        Village thuamul = villages.get(2);
        Village chohtan = villages.get(4);

        List<Infrastructure> items = Arrays.asList(
                Infrastructure.builder()
                        .village(bhamragad)
                        .infraType(InfraType.ROAD)
                        .name("Bhamragad - Hemalkasa Forest Corridor Road")
                        .status(InfraStatus.DAMAGED)
                        .conditionScore(3.2)
                        .latitude(19.6450)
                        .longitude(80.3580)
                        .capacityOrLength("8.5 km")
                        .establishedYear(2014)
                        .schemeName("PMGSY Phase-I")
                        .attributesJson("{\"pavement\":\"unpaved_gravel\",\"pothole_count\":42,\"waterlogging_prone\":true}")
                        .build(),

                Infrastructure.builder()
                        .village(bhamragad)
                        .infraType(InfraType.PRIMARY_HEALTH_CENTRE)
                        .name("Bhamragad Primary Health Centre (PHC)")
                        .status(InfraStatus.OPERATIONAL)
                        .conditionScore(5.5)
                        .latitude(19.6420)
                        .longitude(80.3510)
                        .capacityOrLength("6 beds")
                        .establishedYear(2011)
                        .schemeName("National Health Mission")
                        .attributesJson("{\"doctor_available\":true,\"telemedicine\":false,\"ambulance_on_call\":false}")
                        .build(),

                Infrastructure.builder()
                        .village(thuamul)
                        .infraType(InfraType.PRIMARY_SCHOOL)
                        .name("Govt Tribal Ashram School Thuamul")
                        .status(InfraStatus.OPERATIONAL)
                        .conditionScore(4.8)
                        .latitude(19.5350)
                        .longitude(82.9770)
                        .capacityOrLength("180 students")
                        .establishedYear(2008)
                        .schemeName("Samagra Shiksha Abhiyan")
                        .attributesJson("{\"smart_class\":false,\"drinking_water\":false,\"separate_toilets\":true}")
                        .build(),

                Infrastructure.builder()
                        .village(chohtan)
                        .infraType(InfraType.WATER_OVERHEAD_TANK)
                        .name("Chohtan High-Capacity Desalination Overhead Reservoir")
                        .status(InfraStatus.UNDER_CONSTRUCTION)
                        .conditionScore(8.0)
                        .latitude(25.5690)
                        .longitude(71.0560)
                        .capacityOrLength("150,000 Litres")
                        .establishedYear(2025)
                        .schemeName("Jal Jeevan Mission")
                        .attributesJson("{\"pipeline_coverage_pct\":62,\"sensor_monitoring\":true}")
                        .build()
        );

        infrastructureRepository.saveAll(items);
    }

    private void seedProjectsAndRequirements(List<Village> villages) {
        if (projectRepository.count() > 0) return;

        Village thuamul = villages.get(2);
        Village bhamragad = villages.get(0);

        // 1. Closed-Loop Completed Impact Project: Thuamul Rampur
        Project completedProject = Project.builder()
                .projectCode("GD-OD-2025-001")
                .projectName("Thuamul Rampur Multi-Modal Access & Jal Jeevan Integration")
                .description("Comprehensive all-weather blacktop road connectivity, Ayushman Health Sub-Centre establishment, and solar-powered piped water distribution.")
                .state("Odisha")
                .district("Kalahandi")
                .block("Thuamul Rampur")
                .village(thuamul)
                .villageName(thuamul.getVillageName())
                .latitude(thuamul.getLatitude())
                .longitude(thuamul.getLongitude())
                .projectType("Integrated Multi-Sectoral Infrastructure")
                .priority(PriorityLevel.CRITICAL)
                .status(ProjectStatus.COMPLETED)
                .budgetAllocated(new BigDecimal("48500000.00")) // 4.85 Cr
                .budgetSpent(new BigDecimal("46200000.00"))
                .baselineGapScore(88.0)
                .targetGapScore(35.0)
                .currentGapScore(34.2)
                .gapReductionPct(61.1)
                .impactSummary("Project fully delivered. Reduced village infrastructure gap score from 88.0 (CRITICAL) down to 34.2 (OPTIMAL). 1,890 citizens now have all-weather road transit, 100% tap water coverage, and on-site telemedicine.")
                .startDate(LocalDate.of(2025, 1, 15))
                .targetDate(LocalDate.of(2025, 12, 31))
                .completedAt(LocalDateTime.of(2025, 12, 20, 16, 30))
                .createdBy("pm@gramdrishti.gov.in")
                .deleted(false)
                .build();
        projectRepository.save(completedProject);

        // 2. Active High-Priority Project: Bhamragad Corridor
        Project activeProject = Project.builder()
                .projectCode("GD-MH-2026-002")
                .projectName("Bhamragad Tribal Corridor Road Paving & Telemedicine Sub-Centre")
                .description("PMGSY Phase-III 12.5 km all-weather corridor upgrade and establishment of 24x7 Ayushman Bharat Health & Wellness Kiosk with satellite uplink.")
                .state("Maharashtra")
                .district("Gadchiroli")
                .block("Bhamragad")
                .village(bhamragad)
                .villageName(bhamragad.getVillageName())
                .latitude(bhamragad.getLatitude())
                .longitude(bhamragad.getLongitude())
                .projectType("Road Connectivity & Emergency Healthcare")
                .priority(PriorityLevel.CRITICAL)
                .status(ProjectStatus.ACTIVE)
                .budgetAllocated(new BigDecimal("32000000.00"))
                .budgetSpent(new BigDecimal("14500000.00"))
                .baselineGapScore(86.4)
                .targetGapScore(38.0)
                .currentGapScore(62.0)
                .gapReductionPct(28.2)
                .impactSummary("Phase-1 road sub-base laid; Health sub-centre building 70% complete. Re-evaluation indicates 28.2% gap reduction achieved to date.")
                .startDate(LocalDate.of(2026, 2, 1))
                .targetDate(LocalDate.of(2026, 11, 30))
                .createdBy("pm@gramdrishti.gov.in")
                .deleted(false)
                .build();
        projectRepository.save(activeProject);

        // Requirements
        List<Requirement> reqs = Arrays.asList(
                Requirement.builder()
                        .project(activeProject)
                        .village(bhamragad)
                        .category(RequirementCategory.ROADS)
                        .title("Construct 12.5 km Bituminous All-Weather Road to Bhamragad Highway")
                        .description("Upgrading severely eroded forest corridor to PMGSY-III bituminous standard with 3 box culverts.")
                        .priority(PriorityLevel.CRITICAL)
                        .aiScore(89.5)
                        .source("AI_SATELLITE_SURVEY")
                        .status(RequirementStatus.IN_PROGRESS)
                        .estimatedCost(new BigDecimal("21000000.00"))
                        .suggestedScheme("Pradhan Mantri Gram Sadak Yojana (PMGSY)")
                        .createdBy("analyst@gramdrishti.gov.in")
                        .approvedBy("admin@gramdrishti.gov.in")
                        .approvedAt(LocalDateTime.now().minusDays(20))
                        .build(),

                Requirement.builder()
                        .project(activeProject)
                        .village(bhamragad)
                        .category(RequirementCategory.HEALTHCARE)
                        .title("Deploy Ayushman Bharat Health & Wellness Kiosk with Telemedicine")
                        .description("Solar-powered health diagnostic kiosk with real-time remote physician consultation link.")
                        .priority(PriorityLevel.HIGH)
                        .aiScore(82.0)
                        .source("AI_SURVEY_ANALYSIS")
                        .status(RequirementStatus.APPROVED)
                        .estimatedCost(new BigDecimal("4500000.00"))
                        .suggestedScheme("National Health Mission (NHM)")
                        .createdBy("analyst@gramdrishti.gov.in")
                        .approvedBy("admin@gramdrishti.gov.in")
                        .approvedAt(LocalDateTime.now().minusDays(15))
                        .build(),

                Requirement.builder()
                        .village(villages.get(1)) // Etapalli
                        .category(RequirementCategory.WATER_SANITATION)
                        .title("Install 200 kL Piped Drinking Water System with Solar Pump")
                        .description("Deep borewell with solar submersible pump and automated distribution to 450 households.")
                        .priority(PriorityLevel.HIGH)
                        .aiScore(76.0)
                        .source("FIELD_SURVEY")
                        .status(RequirementStatus.IDENTIFIED)
                        .estimatedCost(new BigDecimal("6800000.00"))
                        .suggestedScheme("Jal Jeevan Mission (Har Ghar Jal)")
                        .createdBy("field@gramdrishti.gov.in")
                        .build()
        );
        requirementRepository.saveAll(reqs);

        // Folders & sample Asset
        Folder folder = Folder.builder()
                .project(activeProject)
                .folderName("Field Drone Surveys & Geotagged Evidence")
                .description("High-resolution orthomosaics and geotagged road progress photos")
                .createdBy("field@gramdrishti.gov.in")
                .deleted(false)
                .build();
        folderRepository.save(folder);

        Asset sampleAsset = Asset.builder()
                .project(activeProject)
                .folder(folder)
                .fileName("bhamragad_corridor_orthomosaic_q1.jpg")
                .fileType("IMAGE")
                .mimeType("image/jpeg")
                .fileSize(4194304L)
                .storageKey("GD-MH-2026-002/evidence_01_bhamragad.jpg")
                .checksumSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .versionNumber(1)
                .status(AssetStatus.ACTIVE)
                .latitude(bhamragad.getLatitude())
                .longitude(bhamragad.getLongitude())
                .isFieldEvidence(true)
                .aiVerificationStatus("RE_SCORING_PROCESSED")
                .uploadedBy("field@gramdrishti.gov.in")
                .deleted(false)
                .build();
        assetRepository.save(sampleAsset);
    }
}
