package com.secureauth.util;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpGeneratorTest {

    @RepeatedTest(20)
    void generatedOtpHasCorrectLength() {
        String otp = OtpGenerator.generateOtp();
        assertEquals(AppConfig.OTP_LENGTH, otp.length());
    }

    @RepeatedTest(20)
    void generatedOtpContainsOnlyDigits() {
        String otp = OtpGenerator.generateOtp();
        assertTrue(otp.matches("^\\d+$"), "OTP should contain only digits: " + otp);
    }

    @Test
    void generatedOtpsAreNotAllZero() {
        boolean allZero = true;
        for (int i = 0; i < 10; i++) {
            if (!OtpGenerator.generateOtp().equals("000000")) {
                allZero = false;
                break;
            }
        }
        assertFalse(allZero, "OTP generator should eventually produce a non-zero OTP");
    }
}
