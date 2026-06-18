package com.secureauth.main;

import com.secureauth.service.AuthService;
import com.secureauth.service.AuthService.*;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Console-based entry point for the Secure Login System.
 * Presents a text menu: Register, Login, Change Password, Exit.
 *
 * This class only handles user I/O and delegates all business
 * logic / validation / persistence to AuthService.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static AuthService authService;

    public static void main(String[] args) {
        printBanner();

        try {
            authService = new AuthService();
        } catch (IllegalStateException e) {
            System.out.println("Startup error: " + e.getMessage());
            return;
        }

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleRegistration();
                    break;
                case "2":
                    handleLogin();
                    break;
                case "3":
                    handleChangePassword();
                    break;
                case "4":
                    running = false;
                    System.out.println("\nThank you for using Secure Login System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, 3 or 4.");
            }
        }
        scanner.close();
    }

    private static void printBanner() {
        System.out.println("==================================================");
        System.out.println("   SECURE LOGIN SYSTEM - EMAIL OTP AUTHENTICATION");
        System.out.println("==================================================");
    }

    private static void printMenu() {
        System.out.println("\n----------- MAIN MENU -----------");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Change Password");
        System.out.println("4. Exit");
        System.out.print("Enter your choice: ");
    }

    /**
     * Reads a password securely using System.console() if available,
     * otherwise falls back to Scanner (useful for IDE consoles).
     */
    private static String readPassword(String prompt) {
        java.io.Console console = System.console();
        if (console != null) {
            char[] passwordChars = console.readPassword(prompt);
            return new String(passwordChars);
        } else {
            System.out.print(prompt);
            return scanner.nextLine();
        }
    }

    // ---------------------------------------------------------------
    // Registration flow
    // ---------------------------------------------------------------

    private static void handleRegistration() {
        System.out.println("\n--- USER REGISTRATION ---");

        System.out.print("Enter username (4-20 chars, letters/digits/underscore): ");
        String username = scanner.nextLine().trim();

        String password = readPassword("Enter password (min 8 chars, at least 1 letter & 1 digit): ").trim();

        System.out.print("Enter email address: ");
        String email = scanner.nextLine().trim();

        RegisterResult result = authService.register(username, password, email);

        switch (result) {
            case SUCCESS:
                System.out.println("✔ Registration successful! You can now log in.");
                break;
            case USERNAME_TAKEN:
                System.out.println("✘ That username is already taken. Please choose another.");
                break;
            case EMAIL_TAKEN:
                System.out.println("✘ That email is already registered.");
                break;
            case INVALID_USERNAME:
                System.out.println("✘ Invalid username format.");
                break;
            case INVALID_PASSWORD:
                System.out.println("✘ Password too weak. Use at least 8 characters with letters and digits.");
                break;
            case INVALID_EMAIL:
                System.out.println("✘ Invalid email format.");
                break;
            case DB_ERROR:
                System.out.println("✘ A database error occurred. Please try again later.");
                break;
        }
    }

    // ---------------------------------------------------------------
    // Login flow: password -> OTP issuance -> OTP verification
    // ---------------------------------------------------------------

    private static void handleLogin() {
        System.out.println("\n--- USER LOGIN ---");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        String password = readPassword("Password: ").trim();

        PasswordCheckResult pwResult = authService.authenticatePassword(username, password);

        switch (pwResult) {
            case USER_NOT_FOUND:
                System.out.println("✘ No such user found. Please register first.");
                return;
            case WRONG_PASSWORD:
                System.out.println("✘ Incorrect password.");
                return;
            case DB_ERROR:
                System.out.println("✘ A database error occurred. Please try again later.");
                return;
            case SUCCESS:
                System.out.println("✔ Password verified. Sending OTP to your registered email...");
                break;
        }

        AtomicInteger otpRecordIdHolder = new AtomicInteger();
        if (!issueOtpAndHandleResult(username, otpRecordIdHolder)) {
            return;
        }

        boolean otpPhase = true;
        while (otpPhase) {
            System.out.println("\n--- OTP VERIFICATION ---");
            System.out.println("1. Enter OTP");
            System.out.println("2. Resend OTP");
            System.out.println("3. Cancel login");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    OtpVerifyResult verifyResult = promptAndVerifyOtp(username);
                    switch (verifyResult) {
                        case VERIFIED:
                            System.out.println("\n✔✔✔ LOGIN SUCCESSFUL! Welcome, " + username + ". ✔✔✔");
                            otpPhase = false;
                            break;
                        case MAX_ATTEMPTS_EXCEEDED:
                            System.out.println("✘ Maximum OTP attempts exceeded. Please log in again to receive a new OTP.");
                            otpPhase = false;
                            break;
                        case EXPIRED:
                            System.out.println("✘ OTP has expired. Please log in again to receive a new OTP.");
                            otpPhase = false;
                            break;
                        case ALREADY_USED:
                            System.out.println("✘ This OTP has already been used. Please log in again.");
                            otpPhase = false;
                            break;
                        case NO_ACTIVE_OTP:
                            System.out.println("✘ No active OTP found. Please log in again.");
                            otpPhase = false;
                            break;
                        case DB_ERROR:
                            System.out.println("✘ A database error occurred during OTP verification.");
                            otpPhase = false;
                            break;
                        case WRONG_OTP:
                            // Allow loop to continue so user can choose again
                            break;
                    }
                    break;
                case "2":
                    System.out.println("Resending OTP...");
                    if (!resendOtpAndHandleResult(username, otpRecordIdHolder)) {
                        otpPhase = false;
                    }
                    break;
                case "3":
                    System.out.println("Login cancelled.");
                    otpPhase = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2 or 3.");
            }
        }
    }

    private static boolean issueOtpAndHandleResult(String username, AtomicInteger otpRecordIdHolder) {
        return doSendOtp(username, otpRecordIdHolder, false);
    }

    private static boolean resendOtpAndHandleResult(String username, AtomicInteger otpRecordIdHolder) {
        return doSendOtp(username, otpRecordIdHolder, true);
    }

    private static boolean doSendOtp(String username, AtomicInteger otpRecordIdHolder, boolean isResend) {
        OtpIssueResult issueResult = isResend
                ? authService.resendOtp(username, otpRecordIdHolder)
                : authService.issueOtp(username, otpRecordIdHolder);

        switch (issueResult) {
            case USER_NOT_FOUND:
                System.out.println("✘ Unexpected error: user disappeared mid-login.");
                return false;
            case EMAIL_FAILED:
                System.out.println("✘ Could not send OTP email. Check your SMTP configuration / internet connection.");
                return false;
            case DB_ERROR:
                System.out.println("✘ A database error occurred while generating OTP.");
                return false;
            case SENT:
                System.out.println("✔ OTP sent! Please check your email (and Spam folder).");
                return true;
        }
        return false;
    }

    private static OtpVerifyResult promptAndVerifyOtp(String username) {
        String otp = readPassword("Enter the 6-digit OTP: ").trim();
        return authService.verifyOtp(username, otp);
    }

    // ---------------------------------------------------------------
    // Change Password flow
    // ---------------------------------------------------------------

    private static void handleChangePassword() {
        System.out.println("\n--- CHANGE PASSWORD ---");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        String oldPassword = readPassword("Current password: ").trim();

        PasswordCheckResult pwResult = authService.authenticatePassword(username, oldPassword);
        switch (pwResult) {
            case USER_NOT_FOUND:
                System.out.println("✘ No such user found.");
                return;
            case WRONG_PASSWORD:
                System.out.println("✘ Incorrect current password.");
                return;
            case DB_ERROR:
                System.out.println("✘ A database error occurred. Please try again later.");
                return;
            case SUCCESS:
                break;
        }

        String newPassword = readPassword("New password (min 8 chars, at least 1 letter & 1 digit): ").trim();
        String confirmPassword = readPassword("Confirm new password: ").trim();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("✘ New passwords do not match.");
            return;
        }

        ChangePasswordResult result = authService.changePassword(username, oldPassword, newPassword);
        switch (result) {
            case SUCCESS:
                System.out.println("✔ Password changed successfully!");
                break;
            case WRONG_OLD_PASSWORD:
                System.out.println("✘ Current password verification failed.");
                break;
            case INVALID_NEW_PASSWORD:
                System.out.println("✘ New password is too weak. Use at least 8 characters with letters and digits.");
                break;
            case SAME_AS_OLD_PASSWORD:
                System.out.println("✘ New password must be different from the old password.");
                break;
            case DB_ERROR:
                System.out.println("✘ A database error occurred. Please try again later.");
                break;
            case USER_NOT_FOUND:
                System.out.println("✘ User not found.");
                break;
        }
    }
}
