package com.secureauth.util;

import java.security.SecureRandom;

/**
 * Generates cryptographically strong numeric OTPs using
 * java.security.SecureRandom (not java.util.Random, which is
 * predictable and unsuitable for security-sensitive code).
 */
public class OtpGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private OtpGenerator() {
    }

    /**
     * Generates a zero-padded numeric OTP of the configured length
     * (default 6 digits, e.g. "042817").
     */
    public static String generateOtp() {
        int length = AppConfig.OTP_LENGTH;
        int bound = (int) Math.pow(10, length);
        int otpValue = SECURE_RANDOM.nextInt(bound);
        return String.format("%0" + length + "d", otpValue);
    }
}
