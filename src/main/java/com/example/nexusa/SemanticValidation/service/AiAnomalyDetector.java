package com.example.nexusa.SemanticValidation.service;

import com.example.nexusa.Dto.GlobalDTOs.HistoricalClaimInput;
import com.example.nexusa.SemanticValidation.graph.HistoricalGraph;
import com.example.nexusa.Model.GlobalModels.AiAnomalySignal;

public interface AiAnomalyDetector {
    AiAnomalySignal inspectClaim(HistoricalClaimInput claim, HistoricalGraph graph);
}
