package com.example.nexusa.CanonicalKnowledge.service;

import com.example.nexusa.CanonicalKnowledge.entity.CanonicalEntity;
import com.example.nexusa.CanonicalKnowledge.entity.CanonicalFact;
import com.example.nexusa.CanonicalKnowledge.entity.ProvenanceRecord;
import com.example.nexusa.Repository.GlobalRepositories.CanonicalEntityRepository;
import com.example.nexusa.Repository.GlobalRepositories.CanonicalFactRepository;
// If you have a separate provenance repository it would be injected here.
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CanonicalizationWorkflowService {

    private final CanonicalEntityRepository entityRepository;
    private final CanonicalFactRepository factRepository;

    public CanonicalizationWorkflowService(CanonicalEntityRepository entityRepository, CanonicalFactRepository factRepository) {
        this.entityRepository = entityRepository;
        this.factRepository = factRepository;
    }

    /**
     * Migration Flow: Promotes an ad-hoc Claim from the Moderation/Research DB 
     * into the Canonical Knowledge base after strict approval.
     */
    @Transactional
    public CanonicalFact promoteClaimToCanonicalFact(UUID subjectEntityId, String statement, String start, String end, Double confidenceScore, UUID originalClaimId, String sourceReference) {
        
        CanonicalEntity entity = entityRepository.findById(subjectEntityId)
            .orElseThrow(() -> new IllegalStateException("Cannot attach fact to non-existent canonical entity"));

        // 1. Create Fact
        CanonicalFact fact = new CanonicalFact();
        fact.setSubjectEntity(entity);
        fact.setStatement(statement);
        fact.setTemporalStart(start);
        fact.setTemporalEnd(end);
        fact.setConfidenceScore(confidenceScore);
        
        CanonicalFact savedFact = factRepository.save(fact);

        // 2. Write Provenance to trace back to research DB claims
        ProvenanceRecord provenance = new ProvenanceRecord();
        provenance.setTargetId(savedFact.getId());
        provenance.setTargetType("FACT");
        provenance.setOriginalClaimId(originalClaimId);
        provenance.setSourceReference(sourceReference);
        // Note: In real app, call provenanceRepository.save(provenance); 

        // 3. Write state to CanonicalVersion for version history

        return savedFact;
    }
}
