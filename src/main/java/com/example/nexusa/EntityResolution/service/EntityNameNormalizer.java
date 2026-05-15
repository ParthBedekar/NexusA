package com.example.nexusa.EntityResolution.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

@Component
public class EntityNameNormalizer {
    private static final Set<String> STOP_WORDS = Set.of("the", "ancient", "kingdom", "empire", "civilization", "civilisation");

    public String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replace("&", " and ")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        StringBuilder result = new StringBuilder();
        for (String token : normalized.split(" ")) {
            if (!token.isBlank() && !STOP_WORDS.contains(token)) {
                if (!result.isEmpty()) {
                    result.append(' ');
                }
                result.append(token);
            }
        }
        return result.toString();
    }
}
