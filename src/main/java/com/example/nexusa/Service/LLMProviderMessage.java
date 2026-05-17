package com.example.nexusa.Service;

public record LLMProviderMessage(String role, String content) {
    public static LLMProviderMessage system(String content) {
        return new LLMProviderMessage("system", content);
    }

    public static LLMProviderMessage user(String content) {
        return new LLMProviderMessage("user", content);
    }

    public static LLMProviderMessage assistant(String content) {
        return new LLMProviderMessage("assistant", content);
    }
}
