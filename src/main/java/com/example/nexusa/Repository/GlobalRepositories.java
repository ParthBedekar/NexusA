package com.example.nexusa.Repository;

import com.example.nexusa.CanonicalKnowledge.entity.CanonicalFact;
import com.example.nexusa.CanonicalKnowledge.entity.CanonicalRelationship;
import com.example.nexusa.ConfidenceScoring.model.ClaimConfidenceScore;
import com.example.nexusa.ConfidenceScoring.model.ScoringWeightConfig;
import com.example.nexusa.ConflictDetection.model.ConflictGroup;
import com.example.nexusa.ConflictDetection.model.ConflictGroupMember;
import com.example.nexusa.EntityResolution.model.DuplicateCandidate;
import com.example.nexusa.EntityResolution.model.EntityAlias;
import com.example.nexusa.EntityResolution.model.EntityMergeAudit;
import com.example.nexusa.KnowledgeGraph.entity.EntityRelationship;
import com.example.nexusa.Model.AdminCodes;
import com.example.nexusa.Model.CVersion;
import com.example.nexusa.Model.CitationSource;
import com.example.nexusa.Model.Civilization;
import com.example.nexusa.Model.ClaimEvidence;
import com.example.nexusa.Model.EditorAssignment;
import com.example.nexusa.Model.Enums.GlobalEnums.CanonicalEntityStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.ClaimStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.ConflictGroupStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.ConflictType;
import com.example.nexusa.Model.Enums.GlobalEnums.DuplicateCandidateStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.EvidenceType;
import com.example.nexusa.Model.Enums.GlobalEnums.ResearchSubmissionStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.Role;
import com.example.nexusa.Model.Enums.GlobalEnums.ScoringConfigStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.SemanticFindingStatus;
import com.example.nexusa.Model.HistoricalClaim;
import com.example.nexusa.Model.ResearchSubmission;
import com.example.nexusa.Model.University;
import com.example.nexusa.Model.UniversityDomain;
import com.example.nexusa.Model.User;
import com.example.nexusa.Model.ValidationLog;
import com.example.nexusa.Moderation.entity.ModerationAuditLog;
import com.example.nexusa.Moderation.entity.ModerationTask;
import com.example.nexusa.Moderation.entity.ModerationTask.TaskStatus;
import com.example.nexusa.SemanticValidation.model.SemanticValidationResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface GlobalRepositories {

    @Repository
    public interface CanonicalEntityRepository extends JpaRepository<com.example.nexusa.CanonicalKnowledge.entity.CanonicalEntity, UUID> {
        
        // Leverage the PostgreSQL TSVECTOR index for fast full-text search across entities
        @Query(value = "SELECT * FROM canonical_entities e WHERE " +
               "e.searchable_text @@ plainto_tsquery('english', :query) " +
               "ORDER BY ts_rank(e.searchable_text, plainto_tsquery('english', :query)) DESC", 
               nativeQuery = true)
        List<com.example.nexusa.CanonicalKnowledge.entity.CanonicalEntity> searchByText(@Param("query") String query);
    }

    @Repository
    public interface CanonicalFactRepository extends JpaRepository<CanonicalFact, UUID> {
        
        List<CanonicalFact> findBySubjectEntityId(UUID subjectEntityId);
    
        // AI Semantic Retrieval via Full Text Search on statements
        @Query(value = "SELECT * FROM canonical_facts f WHERE " +
               "f.searchable_text @@ plainto_tsquery('english', :query) " +
               "ORDER BY f.confidence_score DESC, ts_rank(f.searchable_text, plainto_tsquery('english', :query)) DESC", 
               nativeQuery = true)
        List<CanonicalFact> searchFactsByText(@Param("query") String query);
    }

    @Repository
    public interface CanonicalRelationshipRepository extends JpaRepository<CanonicalRelationship, UUID> {
        List<CanonicalRelationship> findBySourceEntityIdOrTargetEntityId(UUID sourceId, UUID targetId);
    }

    @Repository
    public interface ClaimConfidenceScoreRepository extends JpaRepository<ClaimConfidenceScore, UUID> {
        Optional<ClaimConfidenceScore> findFirstByClaimIdAndSupersededFalseOrderByCalculatedAtDesc(UUID claimId);
    
        List<ClaimConfidenceScore> findByClaimIdOrderByCalculatedAtDesc(UUID claimId);
    
        List<ClaimConfidenceScore> findBySupersededFalseOrderByScoreAsc();
    }

    @Repository
    public interface ScoringWeightConfigRepository extends JpaRepository<ScoringWeightConfig, UUID> {
        Optional<ScoringWeightConfig> findFirstByStatusOrderByVersionDesc(ScoringConfigStatus status);
    
        Optional<ScoringWeightConfig> findByVersion(Integer version);
    }

    @Repository
    public interface ConflictGroupMemberRepository extends JpaRepository<ConflictGroupMember, UUID> {
        List<ConflictGroupMember> findByConflictGroup_ConflictGroupId(UUID conflictGroupId);
    
        List<ConflictGroupMember> findByClaimId(UUID claimId);
    
        boolean existsByConflictGroup_ConflictGroupIdAndClaimId(UUID conflictGroupId, UUID claimId);
    }

    @Repository
    public interface ConflictGroupRepository extends JpaRepository<ConflictGroup, UUID> {
        Optional<ConflictGroup> findFirstBySubjectEntityIdAndPredicateAndConflictTypeAndStatusIn(
                UUID subjectEntityId,
                String predicate,
                ConflictType conflictType,
                List<ConflictGroupStatus> statuses
        );
    
        List<ConflictGroup> findByStatusOrderBySeverityScoreDesc(ConflictGroupStatus status);
    
        List<ConflictGroup> findBySubjectEntityIdOrderBySeverityScoreDesc(UUID subjectEntityId);
    }

    @Repository("entityResolutionCanonicalEntityRepository")
    public interface ResolutionCanonicalEntityRepository extends JpaRepository<com.example.nexusa.EntityResolution.model.CanonicalEntity, UUID> {
        Optional<com.example.nexusa.EntityResolution.model.CanonicalEntity> findByEntityTypeIgnoreCaseAndNormalizedNameAndStatusNot(
                String entityType,
                String normalizedName,
                CanonicalEntityStatus status
        );
    
        List<com.example.nexusa.EntityResolution.model.CanonicalEntity> findByEntityTypeIgnoreCaseAndStatusNot(String entityType, CanonicalEntityStatus status);
    
        @Query(value = """
                SELECT *
                FROM canonical_entities
                WHERE (:entityType IS NULL OR lower(entity_type) = lower(:entityType))
                  AND status <> 'MERGED'
                  AND similarity(normalized_name, :normalizedName) >= :threshold
                ORDER BY similarity(normalized_name, :normalizedName) DESC
                LIMIT :limit
                """, nativeQuery = true)
        List<com.example.nexusa.EntityResolution.model.CanonicalEntity> trigramSearch(@Param("entityType") String entityType,
                                            @Param("normalizedName") String normalizedName,
                                            @Param("threshold") double threshold,
                                            @Param("limit") int limit);
    }

    @Repository
    public interface DuplicateCandidateRepository extends JpaRepository<DuplicateCandidate, UUID> {
        Optional<DuplicateCandidate> findFirstByLeftEntity_CanonicalIdAndRightEntity_CanonicalId(UUID leftId, UUID rightId);
    
        List<DuplicateCandidate> findByStatusOrderBySimilarityScoreDesc(DuplicateCandidateStatus status);
    
        List<DuplicateCandidate> findByLeftEntity_CanonicalIdOrRightEntity_CanonicalIdOrderBySimilarityScoreDesc(UUID leftId, UUID rightId);
    }

    @Repository
    public interface EntityAliasRepository extends JpaRepository<EntityAlias, UUID> {
        List<EntityAlias> findByCanonicalEntity_CanonicalId(UUID canonicalId);
    
        Optional<EntityAlias> findFirstByNormalizedAliasAndCanonicalEntity_EntityTypeIgnoreCase(
                String normalizedAlias,
                String entityType
        );
    
        @Query(value = """
                SELECT a.*
                FROM entity_aliases a
                JOIN canonical_entities c ON c.canonical_id = a.canonical_id
                WHERE (:entityType IS NULL OR lower(c.entity_type) = lower(:entityType))
                  AND c.status <> 'MERGED'
                  AND similarity(a.normalized_alias, :normalizedAlias) >= :threshold
                ORDER BY similarity(a.normalized_alias, :normalizedAlias) DESC
                LIMIT :limit
                """, nativeQuery = true)
        List<EntityAlias> trigramSearch(@Param("entityType") String entityType,
                                        @Param("normalizedAlias") String normalizedAlias,
                                        @Param("threshold") double threshold,
                                        @Param("limit") int limit);
    }

    @Repository
    public interface EntityMergeAuditRepository extends JpaRepository<EntityMergeAudit, UUID> {
        List<EntityMergeAudit> findBySourceCanonicalIdOrTargetCanonicalIdOrderByMergedAtDesc(UUID sourceId, UUID targetId);
    }

    @Repository
    public interface KnowledgeGraphRepository extends JpaRepository<EntityRelationship, UUID> {
        
        // Finds immediate neighbors (Network generation)
        @Query("SELECT r FROM EntityRelationship r WHERE r.sourceEntity.id = :entityId OR r.targetEntity.id = :entityId")
        List<EntityRelationship> findConnections(@Param("entityId") UUID entityId);
    
        // Highly Optimized Graph Traversal using PostgreSQL Recursive CTE
        @Query(value = 
            "WITH RECURSIVE graph_path AS ( " +
            "    SELECT " +
            "        id, source_entity_id, target_entity_id, relationship_type, " +
            "        temporal_start, temporal_end, confidence_score, citations, " +
            "        1 as depth, " +
            "        ARRAY[source_entity_id] as path_visited " +
            "    FROM entity_relationships " +
            "    WHERE source_entity_id = CAST(:sourceId AS UUID) " +
            "    UNION ALL " +
            "    SELECT " +
            "        er.id, er.source_entity_id, er.target_entity_id, er.relationship_type, " +
            "        er.temporal_start, er.temporal_end, er.confidence_score, er.citations, " +
            "        gp.depth + 1, " +
            "        gp.path_visited || er.source_entity_id " +
            "    FROM entity_relationships er " +
            "    INNER JOIN graph_path gp ON er.source_entity_id = gp.target_entity_id " +
            "    WHERE gp.depth < :maxDepth " +
            "    AND NOT (er.target_entity_id = ANY(gp.path_visited)) " + // Cycle prevention check
            ") " +
            "SELECT " +
            "   CAST(id AS VARCHAR) as id, CAST(source_entity_id as VARCHAR) AS sourceId, CAST(target_entity_id as VARCHAR) as targetId, " +
            "   relationship_type as relationshipType, temporal_start as temporalStart, temporal_end as temporalEnd, " +
            "   confidence_score as confidenceScore, depth, path_visited as pathVisited, CAST(citations AS VARCHAR) as citations " +
            "FROM graph_path", nativeQuery = true)
        List<Map<String, Object>> traverseGraphWithCTE(@Param("sourceId") String sourceId, @Param("maxDepth") int maxDepth);
    }

    @Repository
    public interface ModerationAuditLogRepository extends JpaRepository<ModerationAuditLog, UUID> {
        List<ModerationAuditLog> findByTaskIdOrderByTimestampDesc(UUID taskId);
    }

    @Repository
    public interface ModerationTaskRepository extends JpaRepository<ModerationTask, UUID>, JpaSpecificationExecutor<ModerationTask> {
        Page<ModerationTask> findByStatus(TaskStatus status, Pageable pageable);
        Page<ModerationTask> findByAssigneeId(UUID assigneeId, Pageable pageable);
    }

    @Repository
    public interface AdminCodeRepository extends JpaRepository<AdminCodes, UUID> {
       Optional<AdminCodes> findAdminCodesByEmail(String email);
    }

    @Repository
    public interface CitationSourceRepository extends JpaRepository<CitationSource, UUID> {
    
        Page<CitationSource> findByEvidenceType(EvidenceType evidenceType, Pageable pageable);
    
        Optional<CitationSource> findByDoi(String doi);
    
        Optional<CitationSource> findByIsbn(String isbn);
    
        Page<CitationSource> findByReliabilityScoreGreaterThanEqual(Double minScore, Pageable pageable);
    
        Page<CitationSource> findBySubmittedBy_UserId(UUID userId, Pageable pageable);
    }

    @Repository
    public interface CivilizationRepository extends JpaRepository<Civilization, UUID> {
        List<Civilization> findByUniversity_Id(UUID uniId);
    
        // In CivilizationRepository.java
        List<Civilization> findByUniversity(University university);
    }

    @Repository
    public interface ClaimEvidenceRepository extends JpaRepository<ClaimEvidence, UUID> {
    
        Page<ClaimEvidence> findByClaim_ClaimId(UUID claimId, Pageable pageable);
    
        List<ClaimEvidence> findByCitationSource_CitationId(UUID citationSourceId);
    
        Page<ClaimEvidence> findBySubmittedBy_UserId(UUID userId, Pageable pageable);
    
        // Returns average reliability score across all evidence attached to a claim
        @Query("SELECT AVG(e.sourceReliabilityScore) FROM ClaimEvidence e WHERE e.claim.claimId = :claimId AND e.sourceReliabilityScore IS NOT NULL")
        Double findAverageReliabilityScoreByClaimId(UUID claimId);
    }

    @Repository
    public interface CVersionRepository extends JpaRepository<CVersion, UUID> {
        List<CVersion> findByCivilization_CivId(UUID civId);
        Optional<CVersion> findByHash(String hash);
    
        Optional<CVersion> findTopByCivilization_CivIdOrderByCommitTimestampDesc(UUID civId);
    
        Optional<CVersion> findByCivilization_CivIdAndHash(UUID civId, String hash);
        List<CVersion> findByCivilization_CivIdOrderByCommitTimestampDesc(UUID civId);
    }

    @Repository
    public interface EditorAssignmentRepository extends JpaRepository<EditorAssignment, UUID> {
        boolean existsByCivilization_CivIdAndEditor_UserId(UUID civId, UUID userId);
    
        List<EditorAssignment> findByCivilization_CivId(UUID civilizationCivId);
        List<EditorAssignment> findByEditor_UserId(UUID userId);
    }

    @Repository
    public interface HistoricalClaimRepository extends JpaRepository<HistoricalClaim, UUID> {
        
        Page<HistoricalClaim> findBySubmission_SubmissionId(UUID submissionId, Pageable pageable);
        
        Page<HistoricalClaim> findBySubjectEntityId(UUID subjectEntityId, Pageable pageable);
        
        List<HistoricalClaim> findBySubjectEntityIdAndPredicate(UUID subjectEntityId, String predicate);
        
        List<HistoricalClaim> findBySubjectEntityIdAndCanonicalFlagTrue(UUID subjectEntityId);
        
        Page<HistoricalClaim> findByClaimStatus(ClaimStatus status, Pageable pageable);
    }

    @Repository
    public interface ResearchSubmissionRepository extends JpaRepository<ResearchSubmission, UUID>, JpaSpecificationExecutor<ResearchSubmission> {
        
        Page<ResearchSubmission> findByInstitution(University institution, Pageable pageable);
        
        Page<ResearchSubmission> findByResearcher(User researcher, Pageable pageable);
        
        Page<ResearchSubmission> findBySubmissionStatus(ResearchSubmissionStatus status, Pageable pageable);
        
        Page<ResearchSubmission> findByInstitutionAndSubmissionStatus(University institution, ResearchSubmissionStatus status, Pageable pageable);
    }

    @Repository
    public interface UniversityDomainRepository extends JpaRepository<UniversityDomain, UUID> {
        Optional<UniversityDomain> findByDomain(String domain);
    }

    @Repository
    public interface UniversityRepository extends JpaRepository<University, UUID> {
    
    }

    @Repository
    public interface UserRepository extends JpaRepository<User, UUID> {
        Optional<User> findUserByEmail(String email);
    
        Optional<User> findUserByUserId(UUID userId);
    
        List<User> findByUniID(University uniID);
    
        List<User> findUsersByRole(Role role);
    
        boolean existsByEmail(String email);
    
    
    }

    @Repository
    public interface ValidationLogRepository extends JpaRepository<ValidationLog, UUID> {
    
        List<ValidationLog> findBySubmissionIdOrderByValidatedAtDesc(UUID submissionId);
    
        Page<ValidationLog> findByBlockedTrue(Pageable pageable);
    
        Page<ValidationLog> findByPassedFalse(Pageable pageable);
    
        Page<ValidationLog> findByTriggeredBy(UUID userId, Pageable pageable);
    }

    @Repository
    public interface SemanticValidationResultRepository extends JpaRepository<SemanticValidationResult, UUID> {
        List<SemanticValidationResult> findByEntityIdOrderByCreatedAtDesc(String entityId);
    
        List<SemanticValidationResult> findByClaimIdOrderByCreatedAtDesc(String claimId);
    
        List<SemanticValidationResult> findByStatusOrderByCreatedAtDesc(SemanticFindingStatus status);
    }

}
