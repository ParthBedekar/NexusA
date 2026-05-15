package com.example.nexusa.Controller;

import com.example.nexusa.CanonicalKnowledge.service.CanonicalKnowledgeRetrievalService;
import com.example.nexusa.ConfidenceScoring.model.ScoringWeightConfig;
import com.example.nexusa.ConfidenceScoring.service.ConfidenceScoringService;
import com.example.nexusa.ConflictDetection.service.ConflictDetectionService;
import com.example.nexusa.Dto.GlobalDTOs.*;
import com.example.nexusa.Dto.GlobalDTOs.AddNodeRequestDTO;
import com.example.nexusa.Dto.GlobalDTOs.ConfidenceScoreRequest;
import com.example.nexusa.Dto.GlobalDTOs.ConfidenceScoreResponseDTO;
import com.example.nexusa.Dto.GlobalDTOs.ConflictDetectionRequest;
import com.example.nexusa.Dto.GlobalDTOs.ConflictDetectionResponse;
import com.example.nexusa.Dto.GlobalDTOs.ConflictGroupResponseDTO;
import com.example.nexusa.Dto.GlobalDTOs.ConflictModerationRequest;
import com.example.nexusa.Dto.GlobalDTOs.CreateCivilizationDTO;
import com.example.nexusa.Dto.GlobalDTOs.GraphPathResultDTO;
import com.example.nexusa.Dto.GlobalDTOs.HistoricalClaimCreateDTO;
import com.example.nexusa.Dto.GlobalDTOs.HistoricalClaimResponseDTO;
import com.example.nexusa.Dto.GlobalDTOs.HistoricalClaimUpdateDTO;
import com.example.nexusa.Dto.GlobalDTOs.LoginRequestDTO;
import com.example.nexusa.Dto.GlobalDTOs.ModerationActionRequestDTO;
import com.example.nexusa.Dto.GlobalDTOs.ModerationTaskDetailDTO;
import com.example.nexusa.Dto.GlobalDTOs.ModerationTaskListDTO;
import com.example.nexusa.Dto.GlobalDTOs.RecalculateScoresRequest;
import com.example.nexusa.Dto.GlobalDTOs.RegistrationRequestDTO;
import com.example.nexusa.Dto.GlobalDTOs.ResearchSubmissionCreateDTO;
import com.example.nexusa.Dto.GlobalDTOs.ResearchSubmissionResponseDTO;
import com.example.nexusa.Dto.GlobalDTOs.ResearchSubmissionUpdateDTO;
import com.example.nexusa.Dto.GlobalDTOs.RollbackRequestDTO;
import com.example.nexusa.Dto.GlobalDTOs.ScoringWeightsDTO;
import com.example.nexusa.Dto.GlobalDTOs.SemanticGraphValidationRequest;
import com.example.nexusa.Dto.GlobalDTOs.SemanticValidationResponse;
import com.example.nexusa.Dto.GlobalDTOs.ValidationReportResponseDTO;
import com.example.nexusa.Dto.GlobalDTOs.ValidationResultResponseDTO;
import com.example.nexusa.EntityResolution.service.EntityResolutionService;
import com.example.nexusa.KnowledgeGraph.service.GraphTraversalService;
import com.example.nexusa.Model.CVersion;
import com.example.nexusa.Model.Civilization;
import com.example.nexusa.Model.EditorAssignment;
import com.example.nexusa.Model.Enums.GlobalEnums.ClaimStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.ConflictGroupStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.DuplicateCandidateStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.EvidenceType;
import com.example.nexusa.Model.Enums.GlobalEnums.ResearchSubmissionStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.SemanticFindingStatus;
import com.example.nexusa.Model.User;
import com.example.nexusa.Model.ValidationLog;
import com.example.nexusa.Moderation.service.ModerationService;
import com.example.nexusa.SemanticValidation.model.SemanticValidationResult;
import com.example.nexusa.SemanticValidation.service.SemanticValidationService;
import com.example.nexusa.Service.AuthService;
import com.example.nexusa.Service.CitationSourceService;
import com.example.nexusa.Service.CivilizationService;
import com.example.nexusa.Service.ClaimEvidenceService;
import com.example.nexusa.Service.HistoricalClaimService;
import com.example.nexusa.Service.ResearchSubmissionService;
import com.example.nexusa.Service.UniversityService;
import com.example.nexusa.Validation.service.StructuralValidationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.nexusa.Model.GlobalModels.Citation;

public class GlobalControllers {

    @RestController
    @RequestMapping("/api/v1/canonical")
    public static class CanonicalKnowledgeController {
    
        private final CanonicalKnowledgeRetrievalService retrievalService;
    
        public CanonicalKnowledgeController(CanonicalKnowledgeRetrievalService retrievalService) {
            this.retrievalService = retrievalService;
        }
    
        /**
         * AI Application Entry Point
         * Returns a heavily denormalized map of an entity and all verified historical facts.
         */
        @GetMapping("/entities/{id}/context")
        public ResponseEntity<Map<String, Object>> getEntityContextForAI(@PathVariable UUID id) {
            return ResponseEntity.ok(retrievalService.getDenormalizedEntityKnowledge(id));
        }
    
        /**
         * Full Text Semantic Search (TSVECTOR powered)
         */
        @GetMapping("/search/entities")
        public ResponseEntity<List<?>> searchEntities(@RequestParam String query) {
            return ResponseEntity.ok(retrievalService.searchEntities(query));
        }
    
        @GetMapping("/search/facts")
        public ResponseEntity<List<?>> searchFacts(@RequestParam String query) {
            return ResponseEntity.ok(retrievalService.searchFacts(query));
        }
    }

    @RestController
    @RequestMapping("/api/confidence")
    public static class ConfidenceScoringController {
        private final ConfidenceScoringService service;
    
        public ConfidenceScoringController(ConfidenceScoringService service) {
            this.service = service;
        }
    
        @PostMapping("/score")
        public ResponseEntity<ConfidenceScoreResponseDTO> scoreClaim(@RequestBody ConfidenceScoreRequest request) {
            try {
                return ResponseEntity.ok(service.scoreClaim(request));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/recalculate")
        public ResponseEntity<List<ConfidenceScoreResponseDTO>> recalculate(@RequestBody RecalculateScoresRequest request) {
            try {
                return ResponseEntity.ok(service.recalculate(request));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/claim/{claimId}")
        public ResponseEntity<ConfidenceScoreResponseDTO> latestScore(@PathVariable UUID claimId) {
            try {
                return ResponseEntity.ok(service.getLatestScore(claimId));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/claim/{claimId}/history")
        public ResponseEntity<List<ConfidenceScoreResponseDTO>> scoreHistory(@PathVariable UUID claimId) {
            return ResponseEntity.ok(service.getScoreHistory(claimId));
        }
    
        @PostMapping("/weights")
        public ResponseEntity<ScoringWeightConfig> createWeights(@RequestBody ScoringWeightsDTO dto) {
            try {
                return ResponseEntity.ok(service.createWeightConfig(dto));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/weights/active")
        public ResponseEntity<ScoringWeightConfig> activeWeights() {
            return ResponseEntity.ok(service.getActiveConfig());
        }
    }

    @RestController
    @RequestMapping("/api/conflicts")
    public static class ConflictDetectionController {
        private final ConflictDetectionService service;
    
        public ConflictDetectionController(ConflictDetectionService service) {
            this.service = service;
        }
    
        @PostMapping("/detect")
        public ResponseEntity<ConflictDetectionResponse> detectConflicts(@RequestBody ConflictDetectionRequest request) {
            try {
                return ResponseEntity.ok(service.detectConflicts(request));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/detect/claim/{claimId}")
        public ResponseEntity<ConflictDetectionResponse> detectConflictsForClaim(
                @PathVariable UUID claimId,
                @RequestParam(defaultValue = "true") boolean persistGroups) {
            try {
                return ResponseEntity.ok(service.detectConflictsForClaim(claimId, persistGroups));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/queue")
        public ResponseEntity<List<ConflictGroupResponseDTO>> getModerationQueue(
                @RequestParam(required = false) ConflictGroupStatus status) {
            return ResponseEntity.ok(service.getModerationQueue(status));
        }
    
        @GetMapping("/subject/{subjectEntityId}")
        public ResponseEntity<List<ConflictGroupResponseDTO>> getConflictsForSubject(@PathVariable UUID subjectEntityId) {
            return ResponseEntity.ok(service.getConflictsForSubject(subjectEntityId));
        }
    
        @PatchMapping("/{conflictGroupId}/moderate")
        public ResponseEntity<ConflictGroupResponseDTO> moderateConflict(
                @PathVariable UUID conflictGroupId,
                @RequestBody ConflictModerationRequest request) {
            try {
                return ResponseEntity.ok(service.moderateConflict(conflictGroupId, request));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    @RestController
    public static class AuthController {
    
        private final AuthService authService;
        private final UniversityService universityService;
    
        public AuthController(AuthService authService, UniversityService universityService) {
            this.authService = authService;
            this.universityService = universityService;
        }
    
        @PostMapping("/auth/register")
        public ResponseEntity<String> register(@RequestBody RegistrationRequestDTO registrationRequestDTO){
            try{
                String token=authService.register(registrationRequestDTO);
    
                if(token!=null){
                    return ResponseEntity.ok(token);
                }
            }catch (IllegalArgumentException e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.badRequest().body("Registration failed");
        }
        @GetMapping("/auth/universities")
        public ResponseEntity<List<Map<String, String>>> getUniversities() {
            List<Map<String, String>> result = universityService.getAllUniversities();
    
            return ResponseEntity.ok(result);
        }
        @PostMapping("/auth/login")
        public ResponseEntity<String> login(@RequestBody LoginRequestDTO loginRequestDTO){
            try{
                String token=authService.login(loginRequestDTO);
    
                if(token!=null){
                    return ResponseEntity.ok(token);
                }
            }catch (IllegalArgumentException e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.badRequest().body("Login failed");
        }
    
    }

    @RestController
    public static class CivilizationController {
        private final CivilizationService civilizationService;
    
        public CivilizationController(CivilizationService civilizationService) {
            this.civilizationService = civilizationService;
        }
    
        @GetMapping("/civilization/users")
        public ResponseEntity<List<User>> getUniversityUsers() {
            try {
                return ResponseEntity.ok(civilizationService.getUniversityUsers());
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/civilization/create")
        public ResponseEntity<UUID> createCivilization(@RequestBody CreateCivilizationDTO dto) {
            try {
                return ResponseEntity.ok(civilizationService.createCivilization(dto));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/civilization/{civId}/latest")
        public ResponseEntity<CVersion> getLatestVersion(@PathVariable UUID civId) {
            try {
                return ResponseEntity.ok(civilizationService.getLatestVersion(civId));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/civilization/{civId}/versions")
        public ResponseEntity<List<CVersion>> getAllVersions(@PathVariable UUID civId) {
            try {
                return ResponseEntity.ok(civilizationService.getAllVersions(civId));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/civilization/{civId}/editors")
        public ResponseEntity<String> assignEditor(@PathVariable UUID civId, @RequestBody Map<String, UUID> body) {
            try {
                civilizationService.assignEditor(civId, body.get("userId"));
                return ResponseEntity.ok("Editor assigned");
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
    
        @GetMapping("/civilization/{civId}/editors")
        public ResponseEntity<List<EditorAssignment>> getCivilizationEditors(@PathVariable UUID civId) {
            try {
                return ResponseEntity.ok(civilizationService.getCivilizationEditors(civId));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/civilization/my")
        public ResponseEntity<List<Civilization>> getMyCivilizations() {
            try {
                return ResponseEntity.ok(civilizationService.getMyCivilizations());
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/civilization/{civId}/volume")
        public ResponseEntity<CVersion> addVolume(@PathVariable UUID civId,
                                                  @RequestBody AddNodeRequestDTO dto) {
            try {
                return ResponseEntity.ok(civilizationService.addVolume(civId, dto));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/civilization/{civId}/entry")
        public ResponseEntity<CVersion> addEntry(@PathVariable UUID civId,
                                                 @RequestBody AddNodeRequestDTO dto) {
            try {
                return ResponseEntity.ok(civilizationService.addEntry(civId, dto));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PutMapping("/civilization/{civId}/node/{nodeId}")
        public ResponseEntity<CVersion> updateNode(@PathVariable UUID civId,
                                                   @PathVariable UUID nodeId,
                                                   @RequestBody AddNodeRequestDTO dto) {
            try {
                return ResponseEntity.ok(civilizationService.updateNode(civId, nodeId, dto));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/civilization/{civId}/rollback")
        public ResponseEntity<CVersion> rollback(@PathVariable UUID civId,
                                                 @RequestBody RollbackRequestDTO dto) {
            try {
                return ResponseEntity.ok(civilizationService.rollback(civId, dto.getHash()));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(null);
            }
        }
        // In CivilizationController.java
        @GetMapping("/civilization/all")
        public ResponseEntity<List<Civilization>> getAllUniversityCivilizations() {
            try {
                return ResponseEntity.ok(civilizationService.getAllUniversityCivilizations());
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    @RestController
    @RequestMapping("/api/evidence")
    public static class ClaimEvidenceController {
    
        private final ClaimEvidenceService claimEvidenceService;
        private final CitationSourceService citationSourceService;
    
        public ClaimEvidenceController(ClaimEvidenceService claimEvidenceService,
                                       CitationSourceService citationSourceService) {
            this.claimEvidenceService = claimEvidenceService;
            this.citationSourceService = citationSourceService;
        }
    
        // ─── Citation Source Endpoints ─────────────────────────────────────────────
    
        @PostMapping("/sources")
        public ResponseEntity<CitationSourceResponseDTO> createCitationSource(
                @Valid @RequestBody CitationSourceCreateDTO dto) {
            try {
                return ResponseEntity.ok(citationSourceService.createCitationSource(dto));
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/sources/{id}")
        public ResponseEntity<CitationSourceResponseDTO> getCitationSource(@PathVariable UUID id) {
            try {
                return ResponseEntity.ok(citationSourceService.getCitationSource(id));
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }
    
        @GetMapping("/sources")
        public ResponseEntity<Page<CitationSourceResponseDTO>> getCitationSourcesByType(
                @RequestParam EvidenceType type,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            try {
                return ResponseEntity.ok(citationSourceService.getByType(type, PageRequest.of(page, size)));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/sources/reliable")
        public ResponseEntity<Page<CitationSourceResponseDTO>> getHighReliabilitySources(
                @RequestParam(defaultValue = "0.7") Double minScore,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            try {
                return ResponseEntity.ok(
                        citationSourceService.getHighReliabilitySources(minScore, PageRequest.of(page, size)));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PatchMapping("/sources/{id}/reliability")
        public ResponseEntity<CitationSourceResponseDTO> updateReliabilityScore(
                @PathVariable UUID id,
                @RequestParam Double score) {
            try {
                return ResponseEntity.ok(citationSourceService.updateReliabilityScore(id, score));
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        // ─── Claim Evidence Endpoints ──────────────────────────────────────────────
    
        @PostMapping
        public ResponseEntity<ClaimEvidenceResponseDTO> attachEvidence(
                @Valid @RequestBody ClaimEvidenceCreateDTO dto) {
            try {
                return ResponseEntity.ok(claimEvidenceService.attachEvidence(dto));
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/{evidenceId}")
        public ResponseEntity<ClaimEvidenceResponseDTO> getEvidence(@PathVariable UUID evidenceId) {
            try {
                return ResponseEntity.ok(claimEvidenceService.getEvidence(evidenceId));
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }
    
        @GetMapping("/claim/{claimId}")
        public ResponseEntity<Page<ClaimEvidenceResponseDTO>> getEvidenceForClaim(
                @PathVariable UUID claimId,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            try {
                return ResponseEntity.ok(
                        claimEvidenceService.getEvidenceForClaim(
                                claimId, PageRequest.of(page, size, Sort.by("createdAt").descending())));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/claim/{claimId}/reliability")
        public ResponseEntity<Double> getClaimReliabilityScore(@PathVariable UUID claimId) {
            try {
                return ResponseEntity.ok(claimEvidenceService.getClaimEvidenceReliabilityScore(claimId));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @DeleteMapping("/{evidenceId}")
        public ResponseEntity<Void> deleteEvidence(@PathVariable UUID evidenceId) {
            try {
                claimEvidenceService.deleteEvidence(evidenceId);
                return ResponseEntity.ok().build();
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    @RestController
    @RequestMapping("/api/claims")
    public static class HistoricalClaimController {
    
        private final HistoricalClaimService service;
    
        public HistoricalClaimController(HistoricalClaimService service) {
            this.service = service;
        }
    
        @PostMapping
        public ResponseEntity<HistoricalClaimResponseDTO> createClaim(@Valid @RequestBody HistoricalClaimCreateDTO dto) {
            try {
                return ResponseEntity.ok(service.createClaim(dto));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PutMapping("/{id}")
        public ResponseEntity<HistoricalClaimResponseDTO> updateClaim(@PathVariable UUID id, @RequestBody HistoricalClaimUpdateDTO dto) {
            try {
                return ResponseEntity.ok(service.updateClaim(id, dto));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/{id}/moderate")
        public ResponseEntity<HistoricalClaimResponseDTO> moderateClaim(
                @PathVariable UUID id,
                @RequestParam ClaimStatus status,
                @RequestParam(required = false) String notes) {
            try {
                return ResponseEntity.ok(service.moderateClaim(id, status, notes));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/{id}")
        public ResponseEntity<HistoricalClaimResponseDTO> getClaim(@PathVariable UUID id) {
            try {
                return ResponseEntity.ok(service.getClaim(id));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/submission/{submissionId}")
        public ResponseEntity<Page<HistoricalClaimResponseDTO>> getClaimsBySubmission(
                @PathVariable UUID submissionId,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            try {
                return ResponseEntity.ok(service.getClaimsBySubmission(submissionId, PageRequest.of(page, size)));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/subject/{subjectId}")
        public ResponseEntity<Page<HistoricalClaimResponseDTO>> getClaimsBySubject(
                @PathVariable UUID subjectId,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            try {
                return ResponseEntity.ok(service.getClaimsBySubject(subjectId, PageRequest.of(page, size)));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    @RestController
    @RequestMapping("/api/research-submissions")
    public static class ResearchSubmissionController {
    
        private final ResearchSubmissionService service;
    
        public ResearchSubmissionController(ResearchSubmissionService service) {
            this.service = service;
        }
    
        @PostMapping
        public ResponseEntity<ResearchSubmissionResponseDTO> createSubmission(@Valid @RequestBody ResearchSubmissionCreateDTO dto) {
            try {
                return ResponseEntity.ok(service.createSubmission(dto));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PutMapping("/{id}")
        public ResponseEntity<ResearchSubmissionResponseDTO> updateSubmission(@PathVariable UUID id, @RequestBody ResearchSubmissionUpdateDTO dto) {
            try {
                return ResponseEntity.ok(service.updateSubmission(id, dto));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/{id}/submit")
        public ResponseEntity<ResearchSubmissionResponseDTO> submitForReview(@PathVariable UUID id) {
            try {
                return ResponseEntity.ok(service.submitForReview(id));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/{id}/review")
        public ResponseEntity<ResearchSubmissionResponseDTO> reviewSubmission(
                @PathVariable UUID id,
                @RequestParam ResearchSubmissionStatus status,
                @RequestParam(required = false) String remarks) {
            try {
                return ResponseEntity.ok(service.updateStatus(id, status, remarks));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/my")
        public ResponseEntity<Page<ResearchSubmissionResponseDTO>> getMySubmissions(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            try {
                return ResponseEntity.ok(service.getMySubmissions(PageRequest.of(page, size, Sort.by("createdAt").descending())));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/institution")
        public ResponseEntity<Page<ResearchSubmissionResponseDTO>> getInstitutionSubmissions(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            try {
                return ResponseEntity.ok(service.getInstitutionSubmissions(PageRequest.of(page, size, Sort.by("createdAt").descending())));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteSubmission(@PathVariable UUID id) {
            try {
                service.deleteSubmission(id);
                return ResponseEntity.ok().build();
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    @RestController
    @RequestMapping("/api/entity-resolution")
    public static class EntityResolutionController {
        private final EntityResolutionService service;
    
        public EntityResolutionController(EntityResolutionService service) {
            this.service = service;
        }
    
        @PostMapping("/canonical")
        public ResponseEntity<CanonicalEntityResponseDTO> createCanonicalEntity(@Valid @RequestBody CanonicalEntityCreateDTO dto) {
            try {
                return ResponseEntity.ok(service.createCanonicalEntity(dto));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/canonical/{canonicalId}")
        public ResponseEntity<CanonicalEntityResponseDTO> getCanonicalEntity(@PathVariable UUID canonicalId) {
            try {
                return ResponseEntity.ok(service.getCanonicalEntity(canonicalId));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/canonical/{canonicalId}/aliases")
        public ResponseEntity<CanonicalEntityResponseDTO> addAlias(
                @PathVariable UUID canonicalId,
                @Valid @RequestBody EntityAliasCreateDTO dto) {
            try {
                return ResponseEntity.ok(service.addAlias(canonicalId, dto));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/resolve")
        public ResponseEntity<List<EntityMatchDTO>> resolve(@RequestBody EntityResolutionRequest request) {
            try {
                return ResponseEntity.ok(service.resolve(request));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/duplicates/detect")
        public ResponseEntity<List<DuplicateCandidateResponseDTO>> detectDuplicates(@RequestBody DuplicateDetectionRequest request) {
            try {
                return ResponseEntity.ok(service.detectDuplicates(request));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/duplicates")
        public ResponseEntity<List<DuplicateCandidateResponseDTO>> getDuplicateQueue(
                @RequestParam(required = false) DuplicateCandidateStatus status) {
            return ResponseEntity.ok(service.getDuplicateQueue(status));
        }
    
        @PatchMapping("/duplicates/{candidateId}/moderate")
        public ResponseEntity<DuplicateCandidateResponseDTO> moderateDuplicate(
                @PathVariable UUID candidateId,
                @RequestBody ModerateDuplicateRequest request) {
            try {
                return ResponseEntity.ok(service.moderateDuplicate(candidateId, request));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/merge")
        public ResponseEntity<CanonicalEntityResponseDTO> mergeEntities(@RequestBody MergeEntitiesRequest request) {
            try {
                return ResponseEntity.ok(service.mergeEntities(request));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    @RestController
    @RequestMapping("/api/v1/graph")
    public static class KnowledgeGraphController {
    
        private final GraphTraversalService traversalService;
    
        public KnowledgeGraphController(GraphTraversalService traversalService) {
            this.traversalService = traversalService;
        }
    
        /**
         * Traverses outwards from a source canonical entity to a max depth.
         * Perfect for returning sub-graphs/networks for localized visualization dashboards.
         */
        @GetMapping("/traverse/{sourceId}")
        public ResponseEntity<List<GraphPathResultDTO.EdgeDTO>> traverseSubGraph(
                @PathVariable UUID sourceId,
                @RequestParam(defaultValue = "3") int maxDepth) {
            
            return ResponseEntity.ok(traversalService.traverseGraph(sourceId, maxDepth));
        }
    
        /**
         * Attempts to find the connection between two specific historical actors/geographies/concepts.
         * E.g. "How is Augustus connected to Genghis Khan?"
         */
        @GetMapping("/path")
        public ResponseEntity<GraphPathResultDTO> findShortestPath(
                @RequestParam UUID sourceId,
                @RequestParam UUID targetId,
                @RequestParam(defaultValue = "5") int thresholdDepth) {
            
            return ResponseEntity.ok(traversalService.findShortestPath(sourceId, targetId, thresholdDepth));
        }
    }

    @RestController
    @RequestMapping("/api/v1/moderation")
    public static class ModerationController {
    
        private final ModerationService moderationService;
    
        public ModerationController(ModerationService moderationService) {
            this.moderationService = moderationService;
        }
    
        @GetMapping("/tasks")
        @PreAuthorize("hasAnyRole('MODERATOR', 'HISTORIAN', 'ADMIN')")
        public ResponseEntity<Page<ModerationTaskListDTO>> getTasks(Pageable pageable) {
            return ResponseEntity.ok(moderationService.getTasks(pageable));
        }
    
        @GetMapping("/tasks/{id}")
        @PreAuthorize("hasAnyRole('MODERATOR', 'HISTORIAN', 'ADMIN')")
        public ResponseEntity<ModerationTaskDetailDTO> getTaskDetail(@PathVariable UUID id) {
            return ResponseEntity.ok(moderationService.getTaskDetail(id));
        }
    
        @PostMapping("/tasks/{id}/assign")
        @PreAuthorize("hasAnyRole('MODERATOR', 'HISTORIAN', 'ADMIN')")
        public ResponseEntity<Void> assignTask(@PathVariable UUID id, @RequestParam UUID assigneeId) {
            moderationService.assignTask(id, assigneeId);
            return ResponseEntity.ok().build();
        }
    
        @PostMapping("/tasks/{id}/action")
        @PreAuthorize("hasAnyRole('MODERATOR', 'HISTORIAN')")
        public ResponseEntity<Void> executeAction(
                @PathVariable UUID id, 
                @RequestParam UUID moderatorId, 
                @RequestBody ModerationActionRequestDTO request) {
            moderationService.executeAction(id, moderatorId, request);
            // Also emit notification / event here in a real implementation
            return ResponseEntity.ok().build();
        }
    }

    @RestController
    @RequestMapping("/api/semantic-validation")
    public static class SemanticValidationController {
        private final SemanticValidationService service;
    
        public SemanticValidationController(SemanticValidationService service) {
            this.service = service;
        }
    
        @PostMapping("/graph")
        public ResponseEntity<SemanticValidationResponse> validateGraph(@RequestBody SemanticGraphValidationRequest request) {
            try {
                return ResponseEntity.ok(service.validateGraph(request));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/claim/{claimId}")
        public ResponseEntity<SemanticValidationResponse> validateClaim(
                @PathVariable UUID claimId,
                @RequestParam(defaultValue = "true") boolean persistResults) {
            try {
                return ResponseEntity.ok(service.validateClaim(claimId, persistResults));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @PostMapping("/civilization/{civilizationId}")
        public ResponseEntity<SemanticValidationResponse> validateCivilization(
                @PathVariable UUID civilizationId,
                @RequestParam(defaultValue = "true") boolean persistResults) {
            try {
                return ResponseEntity.ok(service.validateCivilization(civilizationId, persistResults));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        @GetMapping("/results")
        public ResponseEntity<List<SemanticValidationResult>> getResults(
                @RequestParam(required = false) String entityId,
                @RequestParam(required = false) String claimId,
                @RequestParam(required = false) SemanticFindingStatus status) {
            return ResponseEntity.ok(service.getResults(entityId, claimId, status));
        }
    
        @PatchMapping("/results/{resultId}/status")
        public ResponseEntity<SemanticValidationResult> updateStatus(
                @PathVariable UUID resultId,
                @RequestParam SemanticFindingStatus status) {
            try {
                return ResponseEntity.ok(service.updateStatus(resultId, status));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    @RestController
    @RequestMapping("/api/validation")
    public static class ValidationController {
    
        private final StructuralValidationService validationService;
    
        public ValidationController(StructuralValidationService validationService) {
            this.validationService = validationService;
        }
    
        /**
         * POST /api/validation/submission/{id}
         * Validates a single research submission through the full rule pipeline.
         * Returns a granular breakdown of errors by severity.
         *
         * Example response:
         * {
         *   "submissionId": "...",
         *   "passed": false,
         *   "blocked": true,
         *   "totalErrors": 3,
         *   "criticalErrors": [{ "ruleCode": "REQUIRED_FIELDS", "fieldPath": "submissionTitle", ... }],
         *   "errors": [],
         *   "warnings": [],
         *   "infos": []
         * }
         */
        @PostMapping("/submission/{id}")
        public ResponseEntity<ValidationResultResponseDTO> validateSubmission(@PathVariable UUID id) {
            try {
                return ResponseEntity.ok(new ValidationResultResponseDTO(validationService.validateSubmission(id)));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        /**
         * POST /api/validation/batch
         * Validates multiple submissions in a single run.
         * Request body: list of submission UUIDs.
         *
         * Example request body: ["uuid1", "uuid2", "uuid3"]
         */
        @PostMapping("/batch")
        public ResponseEntity<ValidationReportResponseDTO> validateBatch(@RequestBody List<UUID> submissionIds) {
            try {
                return ResponseEntity.ok(new ValidationReportResponseDTO(validationService.validateBatch(submissionIds)));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        /**
         * GET /api/validation/logs/submission/{id}
         * Returns all historical validation runs for a given submission (most recent first).
         */
        @GetMapping("/logs/submission/{id}")
        public ResponseEntity<List<ValidationLog>> getLogsForSubmission(@PathVariable UUID id) {
            try {
                return ResponseEntity.ok(validationService.getLogsForSubmission(id));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
        /**
         * GET /api/validation/logs/blocked
         * Returns a paginated list of all validation runs that resulted in a BLOCKED status.
         * Useful for moderator dashboards.
         */
        @GetMapping("/logs/blocked")
        public ResponseEntity<Page<ValidationLog>> getBlockedLogs(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size) {
            try {
                return ResponseEntity.ok(validationService.getBlockedLogs(
                        PageRequest.of(page, size, Sort.by("validatedAt").descending())));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    }

}
