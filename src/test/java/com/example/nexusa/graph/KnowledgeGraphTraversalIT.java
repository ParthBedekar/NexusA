package com.example.nexusa.graph;

import com.example.nexusa.config.BaseIntegrationTest;
import com.example.nexusa.CanonicalKnowledge.entity.CanonicalEntity;
import com.example.nexusa.CanonicalKnowledge.repository.CanonicalEntityRepository;
import com.example.nexusa.KnowledgeGraph.entity.EntityRelationship;
import com.example.nexusa.KnowledgeGraph.entity.RelationshipType;
import com.example.nexusa.KnowledgeGraph.repository.KnowledgeGraphRepository;
import com.example.nexusa.KnowledgeGraph.service.GraphTraversalService;
import com.example.nexusa.KnowledgeGraph.dto.GraphPathResultDTO.EdgeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class KnowledgeGraphTraversalIT extends BaseIntegrationTest {

    @Autowired
    private GraphTraversalService graphTraversalService;

    @Autowired
    private KnowledgeGraphRepository graphRepository;

    @Autowired
    private CanonicalEntityRepository entityRepository;

    private CanonicalEntity genghis;
    private CanonicalEntity kublai;
    private CanonicalEntity marcoPolo;

    @BeforeEach
    void setupGraph() {
        graphRepository.deleteAll();
        entityRepository.deleteAll();

        genghis = new CanonicalEntity();
        genghis.setCanonicalName("Genghis Khan");
        genghis.setEntityType("PERSON");
        genghis = entityRepository.save(genghis);

        kublai = new CanonicalEntity();
        kublai.setCanonicalName("Kublai Khan");
        kublai.setEntityType("PERSON");
        kublai = entityRepository.save(kublai);

        marcoPolo = new CanonicalEntity();
        marcoPolo.setCanonicalName("Marco Polo");
        marcoPolo.setEntityType("PERSON");
        marcoPolo = entityRepository.save(marcoPolo);

        // Edges
        EntityRelationship edge1 = new EntityRelationship();
        edge1.setSourceEntity(genghis);
        edge1.setTargetEntity(kublai);
        edge1.setRelationshipType(RelationshipType.DYNASTY_SUCCESSOR);
        edge1.setConfidenceScore(0.99);
        graphRepository.save(edge1);

        EntityRelationship edge2 = new EntityRelationship();
        edge2.setSourceEntity(kublai);
        edge2.setTargetEntity(marcoPolo);
        edge2.setRelationshipType(RelationshipType.ALLIED_WITH);
        edge2.setConfidenceScore(0.85);
        graphRepository.save(edge2);
    }

    @Test
    void testRecursiveCTEGraphTraversal() {
        // Find everything outwards from Genghis Khan up to depth 3
        List<EdgeDTO> connections = graphTraversalService.traverseGraph(genghis.getId(), 3);

        // We expect Genghis -> Kublai -> Marco Polo (2 total edges traversed)
        assertThat(connections).hasSize(2);
        
        boolean foundMarcoPath = connections.stream()
            .anyMatch(edge -> edge.to.equals(marcoPolo.getId()) && edge.from.equals(kublai.getId()));
            
        assertThat(foundMarcoPath).isTrue();
    }
}
