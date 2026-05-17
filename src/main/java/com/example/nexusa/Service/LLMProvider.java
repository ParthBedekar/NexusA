package com.example.nexusa.Service;

import java.util.List;

public interface LLMProvider {
    String complete(String model, List<LLMProviderMessage> conversation);
}
