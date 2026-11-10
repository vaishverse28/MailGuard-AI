package com.mailguard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mailguard.model.EmailAnalysis;
import com.mailguard.model.EmailStatus;

import java.time.LocalDateTime;
import java.util.List;

/** body fakt "get by id" madhye yeto (history list halka thevnyasathi). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnalyzeResponse(
        Long id,
        String senderEmail,
        String subject,
        String body,
        EmailStatus status,
        int riskScore,
        List<String> suspiciousWords,
        String explanation,
        List<String> recommendations,
        LocalDateTime createdAt
) {
    public static AnalyzeResponse from(EmailAnalysis e, boolean includeBody) {
        return new AnalyzeResponse(
                e.getId(),
                e.getSenderEmail(),
                e.getSubject(),
                includeBody ? e.getBody() : null,
                e.getStatus(),
                e.getRiskScore(),
                e.getSuspiciousWords(),
                e.getExplanation(),
                e.getRecommendations(),
                e.getCreatedAt()
        );
    }
}
