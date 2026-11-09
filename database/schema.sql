-- MailGuard AI - MySQL schema
-- NOTE: Spring Boot (ddl-auto=update) tables auto-create karto.
-- Ha file manually run karaychi asel (ki submission sathi) tar use kar.

CREATE DATABASE IF NOT EXISTS mailguard_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mailguard_db;

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,          -- BCrypt hash
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS email_analysis (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    sender_email      VARCHAR(255) NOT NULL,
    subject           VARCHAR(500) NOT NULL,
    body              LONGTEXT     NOT NULL,
    status            VARCHAR(20)  NOT NULL,    -- SAFE | SPAM | PROMOTIONAL | PHISHING
    risk_score        INT          NOT NULL,    -- 0 - 100
    suspicious_words  TEXT,                     -- JSON array
    explanation       TEXT,
    recommendations   TEXT,                     -- JSON array
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_analysis_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_status (status)
);
