package com.example.nexusa.ConflictDetection;

import com.example.nexusa.ConflictDetection.model.ConflictType;
import com.example.nexusa.ConflictDetection.service.ClaimValueNormalizer;
import com.example.nexusa.ConflictDetection.service.ConflictSeverityScore;
import com.example.nexusa.ConflictDetection.service.ConflictSeverityScorer;
import com.example.nexusa.ConflictDetection.service.NormalizedClaimValue;
import com.example.nexusa.Model.Enums.ClaimType;
import com.example.nexusa.Model.HistoricalClaim;
import com.example.nexusa.Validation.core.ValidationSeverity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConflictComparisonTests {

    @Test
    void normalizesBceDateClaimsAndScoresOneYearConflict() {
        ClaimValueNormalizer normalizer = new ClaimValueNormalizer(new ObjectMapper());
        ConflictSeverityScorer scorer = new ConflictSeverityScorer();

        NormalizedClaimValue left = normalizer.normalize(claim("START_YEAR", "{\"year\":321,\"era\":\"BCE\"}", "321 BCE"));
        NormalizedClaimValue right = normalizer.normalize(claim("START_YEAR", "{\"year\":322,\"era\":\"BCE\"}", "322 BCE"));

        ConflictSeverityScore score = scorer.score(ConflictType.DATE, List.of(left, right));

        assertThat(left.normalizedValue()).isEqualTo("-321");
        assertThat(right.normalizedValue()).isEqualTo("-322");
        assertThat(score.severity()).isEqualTo(ValidationSeverity.WARNING);
        assertThat(score.severityScore()).isGreaterThanOrEqualTo(0.35);
    }

    private HistoricalClaim claim(String predicate, String objectValue, String normalizedValue) {
        HistoricalClaim claim = new HistoricalClaim();
        claim.setClaimId(UUID.randomUUID());
        claim.setSubjectEntityId(UUID.randomUUID());
        claim.setSubjectEntityType("CIVILIZATION");
        claim.setPredicate(predicate);
        claim.setObjectValue(objectValue);
        claim.setNormalizedValue(normalizedValue);
        claim.setClaimType(ClaimType.DATE);
        claim.setConfidenceScore(0.90);
        return claim;
    }
}
