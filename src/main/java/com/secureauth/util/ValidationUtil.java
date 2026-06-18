package com.secureauth.util;

import java.util.regex.Pattern;

/**
 * Simple, dependency-free validators for registration input.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_]{4,20}$");

    private ValidationUtil() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Enforces a minimum password strength: at least 8 characters,
     * containing at least one digit and one letter.
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }

    public static boolean isValidOtpFormat(String otp) {
        return otp != null && otp.trim().matches("^\\d{" + AppConfig.OTP_LENGTH + "}$");
    }
}
