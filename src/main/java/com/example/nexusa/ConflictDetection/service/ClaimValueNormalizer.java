package com.example.nexusa.ConflictDetection.service;

import com.example.nexusa.Model.Enums.GlobalEnums.ConflictType;
import com.example.nexusa.Model.Enums.GlobalEnums.ClaimType;
import com.example.nexusa.Model.HistoricalClaim;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import com.example.nexusa.Model.GlobalModels.NormalizedClaimValue;

@Component
public class ClaimValueNormalizer {
    private final ObjectMapper objectMapper;

    public ClaimValueNormalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public NormalizedClaimValue normalize(HistoricalClaim claim) {
        ConflictType type = inferType(claim);
        String rawValue = extractRawValue(claim, type);
        Long numericValue = parseLong(rawValue);
        String normalized = numericValue == null ? normalizeText(rawValue) : numericValue.toString();
        return new NormalizedClaimValue(type, rawValue, normalized, numericValue);
    }

    public ConflictType inferType(HistoricalClaim claim) {
        String predicate = claim.getPredicate() == null ? "" : claim.getPredicate().toUpperCase(Locale.ROOT);
        ClaimType claimType = claim.getClaimType();

        if (claimType == ClaimType.DATE || predicate.contains("YEAR") || predicate.contains("DATE")) {
            return ConflictType.DATE;
        }
        if (predicate.contains("RULER") || predicate.contains("KING") || predicate.contains("EMPEROR")) {
            return ConflictType.RULER;
        }
        if (claimType == ClaimType.LOCATION || predicate.contains("LOCATION") || predicate.contains("CAPITAL")) {
            return ConflictType.LOCATION;
        }
        if (claimType == ClaimType.ENTITY_REFERENCE || predicate.contains("RULED") ||
                predicate.contains("PART_OF") || predicate.contains("CONTROLLED") || predicate.contains("CONQUERED")) {
            return ConflictType.RELATIONSHIP;
        }
        if (claimType == ClaimType.EVENT || predicate.contains("BATTLE") || predicate.contains("EVENT")) {
            return ConflictType.EVENT;
        }
        return ConflictType.FACT;
    }

    private String extractRawValue(HistoricalClaim claim, ConflictType type) {
        if (claim.getNormalizedValue() != null && !claim.getNormalizedValue().isBlank()) {
            return claim.getNormalizedValue();
        }

        Map<String, Object> json = parseJson(claim.getObjectValue());
        String[] keys = switch (type) {
            case DATE -> new String[]{"year", "value", "startYear", "endYear", "date", "startDate", "endDate"};
            case RULER -> new String[]{"rulerId", "personId", "entityId", "name", "value"};
            case LOCATION -> new String[]{"locationId", "placeId", "entityId", "name", "value"};
            case RELATIONSHIP -> new String[]{"objectEntityId", "entityId", "targetId", "relationship", "value"};
            case EVENT -> new String[]{"eventId", "name", "value", "description"};
            case FACT -> new String[]{"value", "name", "label", "description"};
        };

        for (String key : keys) {
            Object value = json.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return claim.getObjectValue() == null ? "" : claim.getObjectValue();
    }

    private Map<String, Object> parseJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return Map.of("value", rawJson);
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String cleaned = value.trim().toUpperCase(Locale.ROOT)
                .replace("BCE", "")
                .replace("BC", "")
                .replace("CE", "")
                .replace("AD", "")
                .trim();
        try {
            long parsed = Long.parseLong(cleaned.replaceAll("[^0-9-]", ""));
            if (value.toUpperCase(Locale.ROOT).contains("BCE") || value.toUpperCase(Locale.ROOT).contains("BC")) {
                return parsed > 0 ? -parsed : parsed;
            }
            return parsed;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
