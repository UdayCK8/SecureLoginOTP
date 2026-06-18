package com.secureauth.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashAndVerifyRoundtrip() {
        String password = "TestPass123";
        String hash = PasswordUtil.hashPassword(password);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(PasswordUtil.verifyPassword(password, hash));
    }

    @Test
    void wrongPasswordReturnsFalse() {
        String password = "CorrectHorseBatteryStaple";
        String hash = PasswordUtil.hashPassword(password);

        assertFalse(PasswordUtil.verifyPassword("WrongPassword", hash));
    }

    @Test
    void verifyWithNullStoredHashReturnsFalse() {
        assertFalse(PasswordUtil.verifyPassword("any", null));
    }

    @Test
    void verifyWithBlankStoredHashReturnsFalse() {
        assertFalse(PasswordUtil.verifyPassword("any", ""));
    }

    @Test
    void verifyWithNullInputReturnsFalse() {
        String hash = PasswordUtil.hashPassword("password");
        assertFalse(PasswordUtil.verifyPassword(null, hash));
    }

    @Test
    void hashesAreDifferentForSamePassword() {
        String password = "SamePassword42";
        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);

        assertNotEquals(hash1, hash2, "BCrypt should generate unique salts per call");
        assertTrue(PasswordUtil.verifyPassword(password, hash1));
        assertTrue(PasswordUtil.verifyPassword(password, hash2));
    }
}
