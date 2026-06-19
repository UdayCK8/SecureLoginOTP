package com.secureauth.dto;

/**
 * Incoming JSON payload to trigger OTP issuance or resend.
 * The username is pulled from the session instead of the body
 * for security, but this DTO is available if needed.
 */
public class OtpIssueRequest {

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
