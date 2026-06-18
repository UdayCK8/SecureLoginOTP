-- ============================================================
-- Secure Login System Using Email-Based OTP Authentication
-- Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS secure_login_otp
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE secure_login_otp;

-- ------------------------------------------------------------
-- Table: users
-- Stores registered user credentials (passwords are stored
-- as BCrypt hashes, never in plain text).
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    password_salt  VARCHAR(64)  NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_active      BOOLEAN      DEFAULT TRUE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: otp_records
-- Stores OTP issuance/verification history for audit purposes
-- and to enforce expiry + attempt limits.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS otp_records (
    otp_id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL,
    otp_code_hash  VARCHAR(255) NOT NULL,   -- OTP is hashed, not stored in plain text
    generated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at     TIMESTAMP NOT NULL,
    is_used        BOOLEAN DEFAULT FALSE,
    attempt_count  INT DEFAULT 0,
    CONSTRAINT fk_otp_user FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: login_audit  (optional, demonstrates real-world logging)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS login_audit (
    audit_id      INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50),
    attempt_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    stage         VARCHAR(30),     -- 'PASSWORD_CHECK', 'OTP_SENT', 'OTP_VERIFIED', etc.
    result        VARCHAR(20),     -- 'SUCCESS' or 'FAILURE'
    ip_or_host    VARCHAR(100)
) ENGINE=InnoDB;

-- Helpful index for OTP lookups
CREATE INDEX idx_otp_user_unused ON otp_records(user_id, is_used);
