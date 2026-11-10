package com.mailguard.dto;

import com.mailguard.model.EmailStatus;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardStatsResponse(
        long totalScanned,
        long safe,
        long spam,
        long promotional,
        long phishing,
        double threatDetectionRate,
        List<DayStat> weekly,
        List<RecentItem> recent
) {
    public record DayStat(String day, String date, long safe, long spam, long promotional, long phishing) {
    }

    public record RecentItem(Long id, String subject, String sender, EmailStatus status, LocalDateTime createdAt) {
    }
}
