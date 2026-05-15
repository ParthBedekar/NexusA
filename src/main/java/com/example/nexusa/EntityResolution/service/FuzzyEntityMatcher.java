package com.example.nexusa.EntityResolution.service;

import com.example.nexusa.EntityResolution.model.CanonicalEntity;
import com.example.nexusa.EntityResolution.model.EntityAlias;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.example.nexusa.Model.GlobalModels.SimilarityScore;

@Component
public class FuzzyEntityMatcher {
    private final EntityNameNormalizer normalizer;

    public FuzzyEntityMatcher(EntityNameNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public SimilarityScore score(String candidateName,
                                 String candidateType,
                                 Long candidateStartYear,
                                 Long candidateEndYear,
                                 CanonicalEntity entity,
                                 List<EntityAlias> aliases) {
        String normalizedCandidate = normalizer.normalize(candidateName);
        String normalizedCanonical = entity.getNormalizedName();
        double canonicalNameScore = nameScore(normalizedCandidate, normalizedCanonical);

        double bestAliasScore = 0.0;
        boolean aliasMatch = false;
        for (EntityAlias alias : aliases) {
            double score = nameScore(normalizedCandidate, alias.getNormalizedAlias());
            if (score > bestAliasScore) {
                bestAliasScore = score;
            }
            if (normalizedCandidate.equals(alias.getNormalizedAlias())) {
                aliasMatch = true;
            }
        }

        double nameScore = Math.max(canonicalNameScore, bestAliasScore);
        double tokenScore = tokenJaccard(normalizedCandidate, normalizedCanonical);
        double editDistanceScore = editSimilarity(normalizedCandidate, normalizedCanonical);
        double temporalScore = temporalOverlap(candidateStartYear, candidateEndYear, entity.getStartYear(), entity.getEndYear());
        double typeScore = candidateType == null || candidateType.isBlank() || candidateType.equalsIgnoreCase(entity.getEntityType()) ? 1.0 : 0.45;

        double total = (0.42 * nameScore)
                + (0.20 * tokenScore)
                + (0.14 * editDistanceScore)
                + (0.14 * temporalScore)
                + (0.10 * typeScore);
        if (aliasMatch) {
            total = Math.max(total, 0.94);
        }

        List<String> evidence = new ArrayList<>();
        evidence.add("nameScore=" + round(nameScore));
        evidence.add("tokenScore=" + round(tokenScore));
        evidence.add("editDistanceScore=" + round(editDistanceScore));
        evidence.add("temporalScore=" + round(temporalScore));
        evidence.add("typeScore=" + round(typeScore));
        if (aliasMatch) {
            evidence.add("exactAliasMatch=true");
        }

        return new SimilarityScore(round(total), round(nameScore), round(tokenScore), round(editDistanceScore),
                round(temporalScore), aliasMatch, evidence);
    }

    private double nameScore(String left, String right) {
        if (left == null || right == null || left.isBlank() || right.isBlank()) {
            return 0.0;
        }
        if (left.equals(right)) {
            return 1.0;
        }
        if (left.contains(right) || right.contains(left)) {
            return 0.86;
        }
        return Math.max(tokenJaccard(left, right), editSimilarity(left, right));
    }

    private double tokenJaccard(String left, String right) {
        Set<String> leftTokens = tokens(left);
        Set<String> rightTokens = tokens(right);
        if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(leftTokens);
        intersection.retainAll(rightTokens);
        Set<String> union = new HashSet<>(leftTokens);
        union.addAll(rightTokens);
        return (double) intersection.size() / union.size();
    }

    private Set<String> tokens(String value) {
        Set<String> tokens = new HashSet<>();
        if (value == null) {
            return tokens;
        }
        for (String token : value.split(" ")) {
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private double editSimilarity(String left, String right) {
        if (left == null || right == null || left.isBlank() || right.isBlank()) {
            return 0.0;
        }
        int distance = levenshtein(left, right);
        int maxLength = Math.max(left.length(), right.length());
        return maxLength == 0 ? 1.0 : 1.0 - ((double) distance / maxLength);
    }

    private int levenshtein(String left, String right) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] temp = previous;
            previous = current;
            current = temp;
        }
        return previous[right.length()];
    }

    private double temporalOverlap(Long leftStart, Long leftEnd, Long rightStart, Long rightEnd) {
        if (leftStart == null && leftEnd == null || rightStart == null && rightEnd == null) {
            return 0.65;
        }
        if (leftStart == null || leftEnd == null || rightStart == null || rightEnd == null) {
            return 0.55;
        }
        long overlapStart = Math.max(leftStart, rightStart);
        long overlapEnd = Math.min(leftEnd, rightEnd);
        if (overlapStart > overlapEnd) {
            long gap = Math.min(Math.abs(leftStart - rightEnd), Math.abs(rightStart - leftEnd));
            return gap <= 100 ? 0.35 : 0.0;
        }
        long unionStart = Math.min(leftStart, rightStart);
        long unionEnd = Math.max(leftEnd, rightEnd);
        long union = Math.max(1, unionEnd - unionStart);
        return (double) (overlapEnd - overlapStart) / union;
    }

    private double round(double value) {
        return Math.round(Math.max(0.0, Math.min(1.0, value)) * 1000.0) / 1000.0;
    }
}
