package com.mailguard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "email_analysis", indexes = {
        @Index(name = "idx_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class EmailAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "sender_email", nullable = false, length = 255)
    private String senderEmail;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private EmailStatus status;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Convert(converter = StringListConverter.class)
    @Column(name = "suspicious_words", columnDefinition = "TEXT")
    private List<String> suspiciousWords = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> recommendations = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now().withNano(0);
    }
}
