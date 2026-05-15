package com.example.nexusa.SemanticValidation.graph;

import com.example.nexusa.Dto.GlobalDTOs.HistoricalClaimInput;
import com.example.nexusa.Dto.GlobalDTOs.HistoricalEntityNode;
import com.example.nexusa.Dto.GlobalDTOs.HistoricalRelationshipEdge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class HistoricalGraph {
    private final Map<String, HistoricalEntityNode> nodesById = new HashMap<>();
    private final List<HistoricalRelationshipEdge> edges;
    private final List<HistoricalClaimInput> claims;

    public HistoricalGraph(List<HistoricalEntityNode> nodes,
                           List<HistoricalRelationshipEdge> edges,
                           List<HistoricalClaimInput> claims) {
        if (nodes != null) {
            for (HistoricalEntityNode node : nodes) {
                if (node.getId() != null) {
                    nodesById.put(node.getId(), node);
                }
            }
        }
        this.edges = edges == null ? List.of() : List.copyOf(edges);
        this.claims = claims == null ? List.of() : List.copyOf(claims);
    }

    public List<HistoricalEntityNode> nodes() {
        return List.copyOf(nodesById.values());
    }

    public List<HistoricalRelationshipEdge> edges() {
        return edges;
    }

    public List<HistoricalClaimInput> claims() {
        return claims;
    }

    public Optional<HistoricalEntityNode> findNode(String id) {
        return Optional.ofNullable(nodesById.get(id));
    }

    public List<HistoricalRelationshipEdge> outgoing(String nodeId, String type) {
        return edges.stream()
                .filter(edge -> nodeId != null && nodeId.equals(edge.getFromId()))
                .filter(edge -> type == null || type.equalsIgnoreCase(edge.getType()))
                .toList();
    }

    public List<HistoricalRelationshipEdge> incoming(String nodeId, String type) {
        return edges.stream()
                .filter(edge -> nodeId != null && nodeId.equals(edge.getToId()))
                .filter(edge -> type == null || type.equalsIgnoreCase(edge.getType()))
                .toList();
    }

    public List<HistoricalRelationshipEdge> edgesOfType(String type) {
        return edges.stream()
                .filter(edge -> type == null || type.equalsIgnoreCase(edge.getType()))
                .toList();
    }

    public List<HistoricalRelationshipEdge> edgesBetween(String leftId, String rightId) {
        if (leftId == null || rightId == null) {
            return Collections.emptyList();
        }
        return edges.stream()
                .filter(edge -> leftId.equals(edge.getFromId()) && rightId.equals(edge.getToId()))
                .toList();
    }

    public List<HistoricalRelationshipEdge> incidentEdges(String nodeId) {
        if (nodeId == null) {
            return Collections.emptyList();
        }
        List<HistoricalRelationshipEdge> incident = new ArrayList<>();
        for (HistoricalRelationshipEdge edge : edges) {
            if (nodeId.equals(edge.getFromId()) || nodeId.equals(edge.getToId())) {
                incident.add(edge);
            }
        }
        return incident;
    }

    public String normalizedName(HistoricalEntityNode node) {
        if (node == null || node.getName() == null) {
            return "";
        }
        return node.getName().trim().toLowerCase(Locale.ROOT)
                .replace("the ", "")
                .replaceAll("[^a-z0-9 ]", "")
                .replaceAll("\\s+", " ");
    }
}
