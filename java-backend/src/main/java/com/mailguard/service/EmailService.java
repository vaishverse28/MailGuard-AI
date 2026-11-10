package com.mailguard.service;

import com.mailguard.dto.AiAnalysisResult;
import com.mailguard.dto.AnalyzeRequest;
import com.mailguard.dto.AnalyzeResponse;
import com.mailguard.dto.MessageResponse;
import com.mailguard.exception.ApiException;
import com.mailguard.model.EmailAnalysis;
import com.mailguard.model.EmailStatus;
import com.mailguard.model.User;
import com.mailguard.repository.EmailAnalysisRepository;
import com.mailguard.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class EmailService {

    private final EmailAnalysisRepository emailRepository;
    private final UserRepository userRepository;
    private final AiClientService aiClient;

    public EmailService(EmailAnalysisRepository emailRepository,
                        UserRepository userRepository,
                        AiClientService aiClient) {
        this.emailRepository = emailRepository;
        this.userRepository = userRepository;
        this.aiClient = aiClient;
    }

    /**
     * Note: ithe @Transactional mudhdam nahi - AI call 30s paryant lagu shakto,
     * tevha DB connection adakun thevaychi nahi. Fakt save() transactional aahe.
     */
    public AnalyzeResponse analyze(Long userId, AnalyzeRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("User no longer exists. Please login again."));

        AiAnalysisResult ai = aiClient.analyze(req);

        EmailAnalysis entity = new EmailAnalysis();
        entity.setUser(user);
        entity.setSenderEmail(req.senderEmail().trim());
        entity.setSubject(req.subject().trim());
        entity.setBody(req.body());
        entity.setStatus(EmailStatus.valueOf(ai.status()));
        entity.setRiskScore(ai.riskScore());
        entity.setSuspiciousWords(ai.suspiciousWords());
        entity.setExplanation(ai.explanation());
        entity.setRecommendations(ai.recommendations());

        return AnalyzeResponse.from(emailRepository.save(entity), false);
    }

    @Transactional(readOnly = true)
    public List<AnalyzeResponse> history(Long userId, String search, String status, String sort) {
        EmailStatus statusFilter = parseStatus(status);
        Sort sorting = resolveSort(sort);
        return emailRepository.findAll(filter(userId, statusFilter, search), sorting).stream()
                .map(e -> AnalyzeResponse.from(e, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public AnalyzeResponse getById(Long userId, Long id) {
        EmailAnalysis e = emailRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Email analysis with id " + id + " not found"));
        return AnalyzeResponse.from(e, true);
    }

    @Transactional
    public MessageResponse delete(Long userId, Long id) {
        EmailAnalysis e = emailRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Email analysis with id " + id + " not found"));
        emailRepository.delete(e);
        return new MessageResponse(true, "Email deleted successfully");
    }

    // ---------------- helpers ----------------

    private Specification<EmailAnalysis> filter(Long userId, EmailStatus status, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("subject")), like),
                        cb.like(cb.lower(root.get("senderEmail")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private EmailStatus parseStatus(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) {
            return null;
        }
        try {
            return EmailStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Invalid status. Allowed values: SAFE, SPAM, PROMOTIONAL, PHISHING");
        }
    }

    private Sort resolveSort(String sort) {
        String s = (sort == null || sort.isBlank()) ? "latest" : sort.trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "latest", "desc" -> Sort.by(Sort.Direction.DESC, "createdAt", "id");
            case "oldest", "asc" -> Sort.by(Sort.Direction.ASC, "createdAt", "id");
            case "risk_desc", "risk" -> Sort.by(Sort.Direction.DESC, "riskScore", "createdAt");
            case "risk_asc" -> Sort.by(Sort.Direction.ASC, "riskScore", "createdAt");
            default -> throw ApiException.badRequest("Invalid sort. Allowed values: latest, oldest, risk_desc, risk_asc");
        };
    }
}
