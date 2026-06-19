package com.secureauth.dto;

/**
 * Incoming JSON payload for OTP verification (step 3 of login).
 */
public class OtpVerifyRequest {

    private String otp;

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
