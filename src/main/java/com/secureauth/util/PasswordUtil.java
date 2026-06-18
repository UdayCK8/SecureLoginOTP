package com.secureauth.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Provides BCrypt password hashing with an adaptive work factor.
 *
 * BCrypt is strongly preferred over SHA-256 for password storage
 * because it is intentionally slow, making brute-force attacks
 * prohibitively expensive. The salt is embedded in the hash itself,
 * so no separate salt column or management is required.
 */
public class PasswordUtil {

    private static final int BCRYPT_ROUNDS = 12;

    private PasswordUtil() {
    }

    /**
     * Hashes a plain-text password using BCrypt with the configured cost factor.
     *
     * @param password the plain-text password
     * @return the BCrypt hash string (includes salt and cost factor)
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *
     * @param inputPassword the password entered by the user
     * @param storedHash    the BCrypt hash from the database
     * @return true if the password matches the hash
     */
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        if (inputPassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(inputPassword, storedHash);
    }
}
