package com.secureauth.dao;

import com.secureauth.model.OtpRecord;
import com.secureauth.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Data Access Object for the `otp_records` table.
 * Handles persisting OTP hashes, expiry, usage flags and
 * attempt counts.
 */
public class OtpDAO {

    public int insertOtp(int userId, String otpHash, LocalDateTime expiresAt) throws SQLException {
        String sql = "INSERT INTO otp_records (user_id, otp_code_hash, expires_at) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, otpHash);
            ps.setTimestamp(3, Timestamp.valueOf(expiresAt));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Fetches the most recently generated, still-unused OTP record for a user.
     * Expiry check is done in Java (AuthService) to avoid timezone / clock skew
     * between JVM and MySQL causing false negatives.
     */
    public Optional<OtpRecord> findLatestActiveOtp(int userId) throws SQLException {
        String sql = "SELECT otp_id, user_id, otp_code_hash, generated_at, expires_at, is_used, attempt_count " +
                     "FROM otp_records WHERE user_id = ? AND is_used = FALSE " +
                     "ORDER BY generated_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void markUsed(int otpId) throws SQLException {
        String sql = "UPDATE otp_records SET is_used = TRUE WHERE otp_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, otpId);
            ps.executeUpdate();
        }
    }

    public void incrementAttempt(int otpId) throws SQLException {
        String sql = "UPDATE otp_records SET attempt_count = attempt_count + 1 WHERE otp_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, otpId);
            ps.executeUpdate();
        }
    }

    public void deleteExpiredOtps() throws SQLException {
        String sql = "DELETE FROM otp_records WHERE expires_at <= NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    public void markAllUsedForUser(int userId) throws SQLException {
        String sql = "UPDATE otp_records SET is_used = TRUE WHERE user_id = ? AND is_used = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private OtpRecord mapRow(ResultSet rs) throws SQLException {
        OtpRecord record = new OtpRecord();
        record.setOtpId(rs.getInt("otp_id"));
        record.setUserId(rs.getInt("user_id"));
        record.setOtpCodeHash(rs.getString("otp_code_hash"));
        Timestamp gen = rs.getTimestamp("generated_at");
        if (gen != null) record.setGeneratedAt(gen.toLocalDateTime());
        Timestamp exp = rs.getTimestamp("expires_at");
        if (exp != null) record.setExpiresAt(exp.toLocalDateTime());
        record.setUsed(rs.getBoolean("is_used"));
        record.setAttemptCount(rs.getInt("attempt_count"));
        return record;
    }
}
