package com.example.nexusa.SemanticValidation.service;

import com.example.nexusa.Dto.GlobalDTOs.HistoricalClaimInput;
import com.example.nexusa.Dto.GlobalDTOs.HistoricalEntityNode;
import com.example.nexusa.SemanticValidation.graph.HistoricalGraph;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.example.nexusa.Model.GlobalModels.AiAnomalySignal;

@Component
public class HeuristicAiAnomalyDetector implements AiAnomalyDetector {

    @Override
    public AiAnomalySignal inspectClaim(HistoricalClaimInput claim, HistoricalGraph graph) {
        String text = claim.getClaimText() == null ? "" : claim.getClaimText().toLowerCase(Locale.ROOT);
        double score = 0.0;
        String type = "semantic_anomaly";
        String explanation = "No semantic anomaly detected by the local anomaly hook.";

        Optional<HistoricalEntityNode> subject = graph.findNode(claim.getSubjectEntityId());
        Optional<HistoricalEntityNode> object = graph.findNode(claim.getObjectEntityId());

        if (mentionsAnachronisticPair(text)) {
            score = 0.94;
            type = "anachronistic_claim";
            explanation = "The claim contains historically distant entities or periods that are commonly anachronistic together.";
        }

        if (subject.isPresent() && object.isPresent() && hasVeryLargeDistance(subject.get(), object.get())) {
            score = Math.max(score, 0.82);
            type = "suspicious_geography";
            explanation = "The referenced entities are geographically distant and need contact, conquest, or trade evidence.";
        }

        return new AiAnomalySignal(score, type, explanation,
                List.of("temporal_existence", "territorial_control", "source_reliability"));
    }

    private boolean mentionsAnachronisticPair(String text) {
        return (text.contains("roman") && text.contains("aztec"))
                || (text.contains("maurya") && text.contains("britain"))
                || (text.contains("hittite") && text.contains("japan"));
    }

    private boolean hasVeryLargeDistance(HistoricalEntityNode left, HistoricalEntityNode right) {
        if (left.getLatitude() == null || left.getLongitude() == null ||
                right.getLatitude() == null || right.getLongitude() == null) {
            return false;
        }
        double distanceKm = haversineKm(left.getLatitude(), left.getLongitude(), right.getLatitude(), right.getLongitude());
        return distanceKm > 4500;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double radius = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return radius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
