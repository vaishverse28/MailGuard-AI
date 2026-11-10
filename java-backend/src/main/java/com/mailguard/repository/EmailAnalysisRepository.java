package com.mailguard.repository;

import com.mailguard.model.EmailAnalysis;
import com.mailguard.model.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailAnalysisRepository
        extends JpaRepository<EmailAnalysis, Long>, JpaSpecificationExecutor<EmailAnalysis> {

    List<EmailAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<EmailAnalysis> findByStatus(EmailStatus status);

    Optional<EmailAnalysis> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, EmailStatus status);

    List<EmailAnalysis> findByUserIdAndCreatedAtGreaterThanEqual(Long userId, LocalDateTime from);

    List<EmailAnalysis> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
