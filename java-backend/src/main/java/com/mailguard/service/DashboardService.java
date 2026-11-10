package com.mailguard.service;

import com.mailguard.dto.DashboardStatsResponse;
import com.mailguard.dto.DashboardStatsResponse.DayStat;
import com.mailguard.dto.DashboardStatsResponse.RecentItem;
import com.mailguard.model.EmailAnalysis;
import com.mailguard.model.EmailStatus;
import com.mailguard.repository.EmailAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final EmailAnalysisRepository repository;

    public DashboardService(EmailAnalysisRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats(Long userId) {
        long total = repository.countByUserId(userId);
        long safe = repository.countByUserIdAndStatus(userId, EmailStatus.SAFE);
        long spam = repository.countByUserIdAndStatus(userId, EmailStatus.SPAM);
        long promo = repository.countByUserIdAndStatus(userId, EmailStatus.PROMOTIONAL);
        long phishing = repository.countByUserIdAndStatus(userId, EmailStatus.PHISHING);

        // Threat Detection Rate = (SPAM + PHISHING) / total * 100, 1 decimal
        double rate = total == 0 ? 0.0 : Math.round((spam + phishing) * 1000.0 / total) / 10.0;

        return new DashboardStatsResponse(total, safe, spam, promo, phishing, rate,
                weekly(userId), recent(userId));
    }

    /** Last 7 divas (aajcha divas dhaun) - graph sathi. */
    private List<DayStat> weekly(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        Map<LocalDate, List<EmailAnalysis>> byDay = repository
                .findByUserIdAndCreatedAtGreaterThanEqual(userId, start.atStartOfDay())
                .stream()
                .collect(Collectors.groupingBy(e -> e.getCreatedAt().toLocalDate()));

        List<DayStat> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            List<EmailAnalysis> list = byDay.getOrDefault(d, List.of());
            result.add(new DayStat(
                    d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    d.toString(),
                    count(list, EmailStatus.SAFE),
                    count(list, EmailStatus.SPAM),
                    count(list, EmailStatus.PROMOTIONAL),
                    count(list, EmailStatus.PHISHING)));
        }
        return result;
    }

    private List<RecentItem> recent(Long userId) {
        return repository.findTop5ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(e -> new RecentItem(e.getId(), e.getSubject(), e.getSenderEmail(),
                        e.getStatus(), e.getCreatedAt()))
                .toList();
    }

    private long count(List<EmailAnalysis> list, EmailStatus status) {
        return list.stream().filter(e -> e.getStatus() == status).count();
    }
}
