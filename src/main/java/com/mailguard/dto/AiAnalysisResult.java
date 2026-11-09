package com.mailguard.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Response that comes from the Python AI service (Member 3). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiAnalysisResult(
        String status,
        Integer riskScore,
        List<String> suspiciousWords,
        String explanation,
        List<String> recommendations
) {
}
