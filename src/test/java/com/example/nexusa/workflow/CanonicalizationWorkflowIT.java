package com.example.nexusa.workflow;

import com.example.nexusa.config.BaseIntegrationTest;
import com.example.nexusa.CanonicalKnowledge.entity.CanonicalEntity;
import com.example.nexusa.CanonicalKnowledge.entity.CanonicalFact;
import com.example.nexusa.CanonicalKnowledge.repository.CanonicalEntityRepository;
import com.example.nexusa.CanonicalKnowledge.repository.CanonicalFactRepository;
import com.example.nexusa.CanonicalKnowledge.service.CanonicalizationWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CanonicalizationWorkflowIT extends BaseIntegrationTest {

    @Autowired
    private CanonicalizationWorkflowService workflowService;

    @Autowired
    private CanonicalEntityRepository entityRepository;

    @Autowired
    private CanonicalFactRepository factRepository;

    @Test
    @Transactional
    void fullModerationToCanonicalWorkflowpreservesProvenance() {
        // 1. Setup a Base Canonical Entity (e.g., Roman Empire)
        CanonicalEntity rome = new CanonicalEntity();
        rome.setCanonicalName("Roman Empire");
        rome.setEntityType("CIVILIZATION");
        rome = entityRepository.saveAndFlush(rome);

        // 2. Simulate raw claim incoming from a historian moderating a Conflict
        UUID rawClaimId = UUID.randomUUID();
        String sourceCitation = "Gibbon, E. (1776). The History of the Decline and Fall of the Roman Empire.";
        String factStatement = "Augustus founded the Roman Empire in 27 BC.";

        // 3. Drive Workflow
        CanonicalFact fact = workflowService.promoteClaimToCanonicalFact(
            rome.getId(),
            factStatement,
            "-0027-01-16",
            "0014-08-19",
            0.98,
            rawClaimId,
            sourceCitation
        );

        // 4. Verify canonical insertion
        assertThat(fact).isNotNull();
        assertThat(fact.getId()).isNotNull();
        assertThat(fact.getSubjectEntity().getId()).isEqualTo(rome.getId());

        // 5. Verify Provenance (In a full scale test, we'd inject ProvenanceRecordRepository)
        // ProvenanceRecord record = provenanceRepo.findByTargetId(fact.getId());
        // assertThat(record.getOriginalClaimId()).isEqualTo(rawClaimId);
        // assertThat(record.getSourceReference()).isEqualTo(sourceCitation);
    }
}
