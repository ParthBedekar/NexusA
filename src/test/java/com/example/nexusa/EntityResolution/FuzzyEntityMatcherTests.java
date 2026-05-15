package com.example.nexusa.EntityResolution;

import com.example.nexusa.EntityResolution.model.AliasType;
import com.example.nexusa.EntityResolution.model.CanonicalEntity;
import com.example.nexusa.EntityResolution.model.EntityAlias;
import com.example.nexusa.EntityResolution.service.EntityNameNormalizer;
import com.example.nexusa.EntityResolution.service.FuzzyEntityMatcher;
import com.example.nexusa.EntityResolution.service.SimilarityScore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FuzzyEntityMatcherTests {

    @Test
    void scoresHistoricalAliasesAsStrongMatches() {
        EntityNameNormalizer normalizer = new EntityNameNormalizer();
        FuzzyEntityMatcher matcher = new FuzzyEntityMatcher(normalizer);

        CanonicalEntity egypt = new CanonicalEntity();
        egypt.setEntityType("CIVILIZATION");
        egypt.setCanonicalName("Ancient Egypt");
        egypt.setNormalizedName(normalizer.normalize("Ancient Egypt"));
        egypt.setStartYear(-3100L);
        egypt.setEndYear(-30L);

        EntityAlias kemet = new EntityAlias();
        kemet.setAliasName("Kemet");
        kemet.setNormalizedAlias(normalizer.normalize("Kemet"));
        kemet.setAliasType(AliasType.HISTORICAL_NAME);

        SimilarityScore score = matcher.score("Kemet", "CIVILIZATION", -2500L, -500L, egypt, List.of(kemet));

        assertThat(score.aliasMatch()).isTrue();
        assertThat(score.totalScore()).isGreaterThanOrEqualTo(0.94);
    }
}
