package com.example.nexusa.CanonicalKnowledge.service;

import com.example.nexusa.CanonicalKnowledge.entity.CanonicalEntity;
import com.example.nexusa.CanonicalKnowledge.entity.CanonicalFact;
import com.example.nexusa.Repository.GlobalRepositories.CanonicalEntityRepository;
import com.example.nexusa.Repository.GlobalRepositories.CanonicalFactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class CanonicalKnowledgeRetrievalService {

    private final CanonicalEntityRepository entityRepository;
    private final CanonicalFactRepository factRepository;

    public CanonicalKnowledgeRetrievalService(CanonicalEntityRepository entityRepository, CanonicalFactRepository factRepository) {
        this.entityRepository = entityRepository;
        this.factRepository = factRepository;
    }

    /**
     * Highly optimized fetch for AI RAG architecture.
     * Returns a denormalized Map representing the entity, its known aliases, and its validated facts.
     */
    public Map<String, Object> getDenormalizedEntityKnowledge(UUID entityId) {
        CanonicalEntity entity = entityRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Canonical Entity not found"));
        
        List<CanonicalFact> facts = factRepository.findBySubjectEntityId(entityId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", entity.getId());
        response.put("canonicalName", entity.getCanonicalName());
        response.put("type", entity.getEntityType());
        response.put("description", entity.getDescription());
        response.put("temporalSpan", Map.of("start", entity.getTemporalStart(), "end", entity.getTemporalEnd()));
        
        List<String> aliases = entity.getAliases().stream().map(a -> a.getAliasName()).toList();
        response.put("aliases", aliases);

        List<Map<String, Object>> factList = facts.stream().map(f -> {
            Map<String, Object> factMap = new HashMap<>();
            factMap.put("statement", f.getStatement());
            factMap.put("confidence", f.getConfidenceScore());
            factMap.put("span", Map.of("start", f.getTemporalStart(), "end", f.getTemporalEnd()));
            return factMap;
        }).toList();

        response.put("facts", factList);

        return response;
    }

    public List<CanonicalEntity> searchEntities(String query) {
        return entityRepository.searchByText(query);
    }
    
    public List<CanonicalFact> searchFacts(String query) {
        return factRepository.searchFactsByText(query);
    }
}
