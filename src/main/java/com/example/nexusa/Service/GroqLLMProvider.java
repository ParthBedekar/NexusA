package com.example.nexusa.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GroqLLMProvider implements LLMProvider {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String defaultModel;

    public GroqLLMProvider(@Value("${groq.api.key:}") String apiKey,
                           @Value("${groq.model:llama3-8b-8192}") String defaultModel,
                           ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
    }

    @Override
    public String complete(String model, List<LLMProviderMessage> conversation) {
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("\n[GROQ ERROR] API Key is missing or blank in application.properties!\n");
            throw new RuntimeException("Groq API key is not configured.");
        }

        String resolvedModel = (model == null || model.isBlank()) ? defaultModel : model;

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", resolvedModel);
            
            List<Map<String, String>> messages = conversation.stream()
                    .map(msg -> Map.of("role", msg.role(), "content", msg.content()))
                    .toList();
            
            payload.put("messages", messages);
            
            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // --- CRITICAL FIX: CATCH & PRINT ALL GROQ ERRORS LOUDLY ---
            if (response.statusCode() >= 400) {
                System.err.println("\n=========================================================");
                System.err.println("GROQ API CALL FAILED!");
                System.err.println("Status Code: " + response.statusCode());
                System.err.println("Response Body: " + response.body());
                System.err.println("=========================================================\n");
                throw new RuntimeException("Groq API error: " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());

            if (root.has("choices") && root.get("choices").isArray() && !root.get("choices").isEmpty()) {
                JsonNode messageNode = root.get("choices").get(0).get("message");
                if (messageNode != null && messageNode.has("content")) {
                    return messageNode.get("content").asText();
                }
            }

            throw new RuntimeException("Unexpected response from Groq: " + response.body());

        } catch (Exception e) {
            System.err.println("\n[LLM PROVIDER CRASH] " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to call AI Model: " + e.getMessage(), e);
        }
    }
}