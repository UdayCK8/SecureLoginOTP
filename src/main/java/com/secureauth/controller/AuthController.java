package com.secureauth.controller;

import com.secureauth.dto.*;
import com.secureauth.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * REST controller exposing the authentication workflow as HTTP endpoints.
 * <p>
 * Session attributes used:
 * <ul>
 *   <li>{@code PENDING_USERNAME} — set after successful password check</li>
 *   <li>{@code AUTHENTICATED_USER} — set after successful OTP verification</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest request) {
        AuthService.RegisterResult result = authService.register(
                request.getUsername(), request.getPassword(), request.getEmail());

        return switch (result) {
            case SUCCESS -> ResponseEntity.ok(ApiResponse.ok("Registration successful"));
            case USERNAME_TAKEN -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Username is already taken"));
            case EMAIL_TAKEN -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Email is already registered"));
            case INVALID_USERNAME -> ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid username format"));
            case INVALID_PASSWORD -> ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password too weak: min 8 chars with letters and digits"));
            case INVALID_EMAIL -> ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid email format"));
            case DB_ERROR -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error"));
        };
    }

    // ------------------------------------------------------------------
    // Step 1: Password check
    // ------------------------------------------------------------------

    @PostMapping("/login/password")
    public ResponseEntity<ApiResponse<Void>> checkPassword(
            @RequestBody LoginPasswordRequest request, HttpSession session) {

        AuthService.PasswordCheckResult result = authService.authenticatePassword(
                request.getUsername(), request.getPassword());

        return switch (result) {
            case SUCCESS -> {
                session.setAttribute("PENDING_USERNAME", request.getUsername());
                yield ResponseEntity.ok(ApiResponse.ok("Password verified"));
            }
            case USER_NOT_FOUND -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not found"));
            case WRONG_PASSWORD -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Incorrect password"));
            case DB_ERROR -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error"));
        };
    }

    // ------------------------------------------------------------------
    // Step 2a: Issue OTP
    // ------------------------------------------------------------------

    @PostMapping("/login/otp/issue")
    public ResponseEntity<ApiResponse<Void>> issueOtp(HttpSession session) {
        String username = getPendingUsername(session);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Password verification required"));
        }

        AtomicInteger idHolder = new AtomicInteger();
        AuthService.OtpIssueResult result = authService.issueOtp(username, idHolder);

        return switch (result) {
            case SENT -> ResponseEntity.ok(ApiResponse.ok("OTP sent to your email"));
            case USER_NOT_FOUND -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not found"));
            case EMAIL_FAILED -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send OTP email. Check SMTP configuration."));
            case DB_ERROR -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error"));
        };
    }

    // ------------------------------------------------------------------
    // Step 2b: Resend OTP
    // ------------------------------------------------------------------

    @PostMapping("/login/otp/resend")
    public ResponseEntity<ApiResponse<Void>> resendOtp(HttpSession session) {
        String username = getPendingUsername(session);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Password verification required"));
        }

        AtomicInteger idHolder = new AtomicInteger();
        AuthService.OtpIssueResult result = authService.resendOtp(username, idHolder);

        return switch (result) {
            case SENT -> ResponseEntity.ok(ApiResponse.ok("New OTP sent to your email"));
            case USER_NOT_FOUND -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not found"));
            case EMAIL_FAILED -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send OTP email. Check SMTP configuration."));
            case DB_ERROR -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error"));
        };
    }

    // ------------------------------------------------------------------
    // Step 3: Verify OTP
    // ------------------------------------------------------------------

    @PostMapping("/login/otp/verify")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @RequestBody OtpVerifyRequest request, HttpSession session) {

        String username = getPendingUsername(session);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Password verification required"));
        }

        AuthService.OtpVerifyResult result = authService.verifyOtp(username, request.getOtp());

        return switch (result) {
            case VERIFIED -> {
                session.setAttribute("AUTHENTICATED_USER", username);
                session.removeAttribute("PENDING_USERNAME");
                yield ResponseEntity.ok(ApiResponse.ok("Login successful"));
            }
            case WRONG_OTP -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid OTP"));
            case MAX_ATTEMPTS_EXCEEDED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Maximum OTP attempts exceeded. Please restart login."));
            case EXPIRED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("OTP has expired. Please request a new one."));
            case ALREADY_USED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("OTP already used. Please request a new one."));
            case NO_ACTIVE_OTP -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No active OTP found. Please restart login."));
            case DB_ERROR -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error"));
        };
    }

    // ------------------------------------------------------------------
    // Change Password
    // ------------------------------------------------------------------

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody ChangePasswordRequest request) {

        AuthService.ChangePasswordResult result = authService.changePassword(
                request.getUsername(), request.getOldPassword(), request.getNewPassword());

        return switch (result) {
            case SUCCESS -> ResponseEntity.ok(ApiResponse.ok("Password changed successfully"));
            case USER_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            case WRONG_OLD_PASSWORD -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Current password is incorrect"));
            case INVALID_NEW_PASSWORD -> ResponseEntity.badRequest()
                    .body(ApiResponse.error("New password too weak: min 8 chars with letters and digits"));
            case SAME_AS_OLD_PASSWORD -> ResponseEntity.badRequest()
                    .body(ApiResponse.error("New password must be different from the old password"));
            case DB_ERROR -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database error"));
        };
    }

    // ------------------------------------------------------------------
    // Logout
    // ------------------------------------------------------------------

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.ok("Logged out"));
    }

    // ------------------------------------------------------------------
    // Me
    // ------------------------------------------------------------------

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> me(HttpSession session) {
        String user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Authenticated", user));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String getPendingUsername(HttpSession session) {
        return (String) session.getAttribute("PENDING_USERNAME");
    }

    private String getAuthenticatedUser(HttpSession session) {
        return (String) session.getAttribute("AUTHENTICATED_USER");
    }
}
