package com.secureauth.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    // -- Email --
    @Test
    void validEmailsAccepted() {
        assertTrue(ValidationUtil.isValidEmail("user@example.com"));
        assertTrue(ValidationUtil.isValidEmail("first.last@domain.co.in"));
        assertTrue(ValidationUtil.isValidEmail("user+tag@example.org"));
    }

    @Test
    void invalidEmailsRejected() {
        assertFalse(ValidationUtil.isValidEmail("plainaddress"));
        assertFalse(ValidationUtil.isValidEmail("@missinglocal.com"));
        assertFalse(ValidationUtil.isValidEmail("missing@tld"));
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail(""));
    }

    // -- Username --
    @Test
    void validUsernamesAccepted() {
        assertTrue(ValidationUtil.isValidUsername("john_doe"));
        assertTrue(ValidationUtil.isValidUsername("user123"));
        assertTrue(ValidationUtil.isValidUsername("aB_9"));
    }

    @Test
    void invalidUsernamesRejected() {
        assertFalse(ValidationUtil.isValidUsername("ab"));           // too short
        assertFalse(ValidationUtil.isValidUsername("user@name"));    // invalid char
        assertFalse(ValidationUtil.isValidUsername("verylongusernamethatexceedstwentylimit"));
        assertFalse(ValidationUtil.isValidUsername(null));
        assertFalse(ValidationUtil.isValidUsername(""));
    }

    // -- Password --
    @Test
    void strongPasswordsAccepted() {
        assertTrue(ValidationUtil.isStrongPassword("abcd1234"));
        assertTrue(ValidationUtil.isStrongPassword("MyP@ssw0rd"));
        assertTrue(ValidationUtil.isStrongPassword("Aa0" + "x".repeat(20)));
    }

    @Test
    void weakPasswordsRejected() {
        assertFalse(ValidationUtil.isStrongPassword("short1"));       // < 8
        assertFalse(ValidationUtil.isStrongPassword("onlyletters"));  // no digit
        assertFalse(ValidationUtil.isStrongPassword("12345678"));     // no letter
        assertFalse(ValidationUtil.isStrongPassword(null));
        assertFalse(ValidationUtil.isStrongPassword(""));
    }

    // -- OTP --
    @Test
    void validOtpsAccepted() {
        assertTrue(ValidationUtil.isValidOtpFormat("123456"));
        assertTrue(ValidationUtil.isValidOtpFormat("000000"));
        assertTrue(ValidationUtil.isValidOtpFormat("987654"));
    }

    @Test
    void invalidOtpsRejected() {
        assertFalse(ValidationUtil.isValidOtpFormat("12345"));        // too short
        assertFalse(ValidationUtil.isValidOtpFormat("1234567"));      // too long
        assertFalse(ValidationUtil.isValidOtpFormat("12a456"));       // non-digit
        assertFalse(ValidationUtil.isValidOtpFormat(null));
        assertFalse(ValidationUtil.isValidOtpFormat(""));
    }
}
