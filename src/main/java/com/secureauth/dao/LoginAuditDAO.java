package com.secureauth.dao;

import com.secureauth.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Lightweight audit logger — records each stage of the login
 * workflow (password check, OTP sent, OTP verified, etc.) for
 * traceability. This is a common real-world security practice
 * (useful to mention in the viva: detecting brute-force patterns).
 */
public class LoginAuditDAO {

    public void log(String username, String stage, String result) {
        String sql = "INSERT INTO login_audit (username, stage, result, ip_or_host) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, stage);
            ps.setString(3, result);
            ps.setString(4, safeLocalHost());
            ps.executeUpdate();
        } catch (SQLException e) {
            // Audit logging should never crash the main flow.
            System.err.println("Audit log failed: " + e.getMessage());
        }
    }

    private String safeLocalHost() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }
}
