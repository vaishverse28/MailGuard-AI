package com.mailguard.service;

import com.mailguard.dto.AiAnalysisResult;
import com.mailguard.dto.AnalyzeRequest;
import com.mailguard.exception.ApiException;
import com.mailguard.model.EmailStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Python AI service (Member 3) la call karto.
 * ai.service.mock=true asel tar dummy/keyword based response deto (AI ready nasel tevha).
 */
@Service
public class AiClientService {

    private static final Logger log = LoggerFactory.getLogger(AiClientService.class);

    private final RestTemplate restTemplate;
    private final String aiBaseUrl;
    private final boolean mock;

    public AiClientService(RestTemplate aiRestTemplate,
                           @Value("${ai.service.url}") String aiBaseUrl,
                           @Value("${ai.service.mock:false}") boolean mock) {
        this.restTemplate = aiRestTemplate;
        this.aiBaseUrl = aiBaseUrl.endsWith("/") ? aiBaseUrl.substring(0, aiBaseUrl.length() - 1) : aiBaseUrl;
        this.mock = mock;
    }

    public AiAnalysisResult analyze(AnalyzeRequest request) {
        if (mock) {
            log.warn("AI mock mode ON - dummy response vaparla jat aahe (ai.service.mock=true)");
            return mockAnalyze(request);
        }

        AiAnalysisResult raw;
        try {
            raw = restTemplate.postForObject(aiBaseUrl + "/analyze", request, AiAnalysisResult.class);
        } catch (ResourceAccessException e) {
            log.error("AI service not reachable / timed out: {}", e.getMessage());
            throw ApiException.serviceUnavailable("AI service is currently unavailable or timed out. Please try again later.");
        } catch (RestClientException e) {
            log.error("AI service returned an error: {}", e.getMessage());
            throw ApiException.serviceUnavailable("AI service returned an error. Please try again later.");
        }
        return normalize(raw);
    }

    /** AI response validate + clean kar (status valid, score 0-100, lists null nahi). */
    private AiAnalysisResult normalize(AiAnalysisResult raw) {
        if (raw == null || raw.status() == null) {
            throw ApiException.serviceUnavailable("AI service returned an empty or invalid response.");
        }
        String status = raw.status().trim().toUpperCase(Locale.ROOT);
        try {
            EmailStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            log.error("AI returned unknown status: {}", raw.status());
            throw ApiException.serviceUnavailable("AI service returned an unknown status.");
        }
        int score = raw.riskScore() == null ? 0 : Math.max(0, Math.min(100, raw.riskScore()));
        return new AiAnalysisResult(
                status,
                score,
                raw.suspiciousWords() == null ? List.of() : raw.suspiciousWords(),
                raw.explanation() == null ? "" : raw.explanation(),
                raw.recommendations() == null ? List.of() : raw.recommendations()
        );
    }

    // ------------------------------------------------------------------
    // Mock AI (simple keyword rules) - fakt development/testing sathi
    // ------------------------------------------------------------------
    private AiAnalysisResult mockAnalyze(AnalyzeRequest r) {
        String text = (r.subject() + " " + r.body()).toLowerCase(Locale.ROOT);
        String sender = r.senderEmail().toLowerCase(Locale.ROOT);
        String domain = sender.contains("@") ? sender.substring(sender.indexOf('@') + 1) : sender;

        List<String> phishingHits = hits(text, "verify your account", "urgent", "suspended", "click here",
                "password", "confirm your identity", "unusual activity", "account will be");
        List<String> spamHits = hits(text, "winner", "lottery", "free money", "claim your prize",
                "congratulations", "act now", "100% free", "limited time");
        List<String> promoHits = hits(text, "sale", "discount", "% off", "offer", "coupon",
                "unsubscribe", "deal", "shop now");
        boolean fakeDomain = domain.matches(".*\\d.*") || domain.contains("-security") || domain.contains("-verify");

        if (phishingHits.size() >= 2 || (!phishingHits.isEmpty() && fakeDomain)) {
            int score = Math.min(98, 55 + phishingHits.size() * 10 + (fakeDomain ? 15 : 0));
            return new AiAnalysisResult("PHISHING", score, phishingHits,
                    "Email creates urgency and asks for account verification" +
                            (fakeDomain ? ", and the sender domain '" + domain + "' looks suspicious." : "."),
                    List.of("Do not click any links.", "Never share your password.",
                            "Verify the sender before replying."));
        }
        if (!spamHits.isEmpty()) {
            int score = Math.min(90, 50 + spamHits.size() * 12);
            return new AiAnalysisResult("SPAM", score, spamHits,
                    "Email contains typical spam phrases promising prizes or money.",
                    List.of("Do not reply to this email.", "Mark it as spam and delete it."));
        }
        if (!promoHits.isEmpty()) {
            int score = Math.min(45, 20 + promoHits.size() * 5);
            return new AiAnalysisResult("PROMOTIONAL", score, promoHits,
                    "Email looks like a marketing / promotional message.",
                    List.of("Unsubscribe if you are not interested."));
        }
        return new AiAnalysisResult("SAFE", 5, new ArrayList<>(),
                "No suspicious patterns were found in this email.",
                List.of("No action needed."));
    }

    private List<String> hits(String text, String... keywords) {
        List<String> found = new ArrayList<>();
        for (String k : keywords) {
            if (text.contains(k)) {
                found.add(k);
            }
        }
        return found;
    }
}
