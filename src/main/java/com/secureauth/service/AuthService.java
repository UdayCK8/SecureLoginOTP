package com.secureauth.service;

import com.secureauth.dao.LoginAuditDAO;
import com.secureauth.dao.OtpDAO;
import com.secureauth.dao.UserDAO;
import com.secureauth.model.OtpRecord;
import com.secureauth.model.User;
import com.secureauth.util.*;

import jakarta.mail.MessagingException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core orchestration layer implementing the project's workflow:
 *
 *   1. register()        -> validate input, hash password, store user
 *   2. authenticatePassword() -> verify username/password (step 1 of login)
 *   3. issueOtp()         -> generate OTP, hash + store it, email the plain OTP
 *   4. verifyOtp()        -> compare submitted OTP against stored hash,
 *                            enforcing expiry and max-attempt limits
 *   5. changePassword()   -> verify old password, update to new hashed password
 *   6. resendOtp()        -> invalidate previous OTP and issue a fresh one
 *
 * This class deliberately contains no console I/O (System.out/Scanner) —
 * that belongs to the presentation layer (Main / Menu classes) so the
 * business logic stays testable and reusable.
 */
public class AuthService {

    private final UserDAO userDAO;
    private final OtpDAO otpDAO;
    private final LoginAuditDAO auditDAO;
    private final EmailService emailService;

    public AuthService() {
        this.userDAO = new UserDAO();
        this.otpDAO = new OtpDAO();
        this.auditDAO = new LoginAuditDAO();
        this.emailService = new EmailService();
    }

    /** Package-private constructor for unit tests with mock DAOs. */
    AuthService(UserDAO userDAO, OtpDAO otpDAO, LoginAuditDAO auditDAO, EmailService emailService) {
        this.userDAO = userDAO;
        this.otpDAO = otpDAO;
        this.auditDAO = auditDAO;
        this.emailService = emailService;
    }

    // ---------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------

    public enum RegisterResult {
        SUCCESS, USERNAME_TAKEN, EMAIL_TAKEN, INVALID_USERNAME, INVALID_PASSWORD, INVALID_EMAIL, DB_ERROR
    }

    public RegisterResult register(String username, String password, String email) {
        if (!ValidationUtil.isValidUsername(username)) {
            return RegisterResult.INVALID_USERNAME;
        }
        if (!ValidationUtil.isStrongPassword(password)) {
            return RegisterResult.INVALID_PASSWORD;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return RegisterResult.INVALID_EMAIL;
        }

        try {
            if (userDAO.usernameExists(username)) {
                return RegisterResult.USERNAME_TAKEN;
            }
            if (userDAO.emailExists(email)) {
                return RegisterResult.EMAIL_TAKEN;
            }

            String hash = PasswordUtil.hashPassword(password);
            User newUser = new User(username, hash, "", email);
            int userId = userDAO.insertUser(newUser);

            return userId > 0 ? RegisterResult.SUCCESS : RegisterResult.DB_ERROR;
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            return RegisterResult.DB_ERROR;
        }
    }

    // ---------------------------------------------------------------
    // Step 1 of login: username + password
    // ---------------------------------------------------------------

    public enum PasswordCheckResult {
        SUCCESS, USER_NOT_FOUND, WRONG_PASSWORD, DB_ERROR
    }

    public PasswordCheckResult authenticatePassword(String username, String password) {
        try {
            Optional<User> userOpt = userDAO.findByUsername(username);
            if (userOpt.isEmpty()) {
                auditDAO.log(username, "PASSWORD_CHECK", "FAILURE");
                return PasswordCheckResult.USER_NOT_FOUND;
            }

            User user = userOpt.get();
            boolean matches = PasswordUtil.verifyPassword(password, user.getPasswordHash());

            auditDAO.log(username, "PASSWORD_CHECK", matches ? "SUCCESS" : "FAILURE");
            return matches ? PasswordCheckResult.SUCCESS : PasswordCheckResult.WRONG_PASSWORD;

        } catch (SQLException e) {
            System.err.println("Database error during password check: " + e.getMessage());
            return PasswordCheckResult.DB_ERROR;
        }
    }

    // ---------------------------------------------------------------
    // Step 2 of login: OTP issuance
    // ---------------------------------------------------------------

    public enum OtpIssueResult {
        SENT, USER_NOT_FOUND, EMAIL_FAILED, DB_ERROR
    }

    /**
     * Generates a new OTP for the given username, stores its hash
     * with an expiry timestamp, and emails the plain OTP to the
     * user's registered address.
     */
    public OtpIssueResult issueOtp(String username, AtomicInteger otpRecordIdOut) {
        try {
            Optional<User> userOpt = userDAO.findByUsername(username);
            if (userOpt.isEmpty()) {
                return OtpIssueResult.USER_NOT_FOUND;
            }
            User user = userOpt.get();

            otpDAO.deleteExpiredOtps();

            String otp = OtpGenerator.generateOtp();
            String otpHash = PasswordUtil.hashPassword(otp);

            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(AppConfig.OTP_VALID_MINUTES);
            int otpId = otpDAO.insertOtp(user.getUserId(), otpHash, expiresAt);

            if (otpId <= 0) {
                return OtpIssueResult.DB_ERROR;
            }

            try {
                emailService.sendOtpEmail(user.getEmail(), otp, AppConfig.OTP_VALID_MINUTES);
            } catch (MessagingException e) {
                System.err.println("Failed to send OTP email: " + e.getMessage());
                auditDAO.log(username, "OTP_SENT", "FAILURE");
                return OtpIssueResult.EMAIL_FAILED;
            }

            auditDAO.log(username, "OTP_SENT", "SUCCESS");
            if (otpRecordIdOut != null) {
                otpRecordIdOut.set(otpId);
            }
            return OtpIssueResult.SENT;

        } catch (SQLException e) {
            System.err.println("Database error during OTP issuance: " + e.getMessage());
            return OtpIssueResult.DB_ERROR;
        }
    }

    // ---------------------------------------------------------------
    // Resend OTP: invalidate previous, issue a fresh one
    // ---------------------------------------------------------------

    public OtpIssueResult resendOtp(String username, AtomicInteger otpRecordIdOut) {
        try {
            Optional<User> userOpt = userDAO.findByUsername(username);
            if (userOpt.isEmpty()) {
                return OtpIssueResult.USER_NOT_FOUND;
            }
            User user = userOpt.get();

            otpDAO.markAllUsedForUser(user.getUserId());
            auditDAO.log(username, "OTP_RESENT", "SUCCESS");

            return issueOtp(username, otpRecordIdOut);

        } catch (SQLException e) {
            System.err.println("Database error during OTP resend: " + e.getMessage());
            return OtpIssueResult.DB_ERROR;
        }
    }

    // ---------------------------------------------------------------
    // Step 3 of login: OTP verification
    // ---------------------------------------------------------------

    public enum OtpVerifyResult {
        VERIFIED, EXPIRED, ALREADY_USED, WRONG_OTP, MAX_ATTEMPTS_EXCEEDED, NO_ACTIVE_OTP, DB_ERROR
    }

    public OtpVerifyResult verifyOtp(String username, String submittedOtp) {
        try {
            Optional<User> userOpt = userDAO.findByUsername(username);
            if (userOpt.isEmpty()) {
                return OtpVerifyResult.NO_ACTIVE_OTP;
            }
            User user = userOpt.get();

            Optional<OtpRecord> recordOpt = otpDAO.findLatestActiveOtp(user.getUserId());
            if (recordOpt.isEmpty()) {
                return OtpVerifyResult.NO_ACTIVE_OTP;
            }
            OtpRecord record = recordOpt.get();

            if (record.getAttemptCount() >= AppConfig.OTP_MAX_ATTEMPTS) {
                auditDAO.log(username, "OTP_VERIFIED", "FAILURE");
                return OtpVerifyResult.MAX_ATTEMPTS_EXCEEDED;
            }

            if (LocalDateTime.now().isAfter(record.getExpiresAt())) {
                auditDAO.log(username, "OTP_VERIFIED", "FAILURE");
                return OtpVerifyResult.EXPIRED;
            }

            if (!ValidationUtil.isValidOtpFormat(submittedOtp)) {
                otpDAO.incrementAttempt(record.getOtpId());
                auditDAO.log(username, "OTP_VERIFIED", "FAILURE");
                return OtpVerifyResult.WRONG_OTP;
            }

            boolean matches = PasswordUtil.verifyPassword(submittedOtp.trim(), record.getOtpCodeHash());

            if (matches) {
                otpDAO.markUsed(record.getOtpId());
                auditDAO.log(username, "OTP_VERIFIED", "SUCCESS");
                return OtpVerifyResult.VERIFIED;
            } else {
                otpDAO.incrementAttempt(record.getOtpId());
                auditDAO.log(username, "OTP_VERIFIED", "FAILURE");
                return OtpVerifyResult.WRONG_OTP;
            }

        } catch (SQLException e) {
            System.err.println("Database error during OTP verification: " + e.getMessage());
            return OtpVerifyResult.DB_ERROR;
        }
    }

    // ---------------------------------------------------------------
    // Change Password
    // ---------------------------------------------------------------

    public enum ChangePasswordResult {
        SUCCESS, USER_NOT_FOUND, WRONG_OLD_PASSWORD, INVALID_NEW_PASSWORD, SAME_AS_OLD_PASSWORD, DB_ERROR
    }

    public ChangePasswordResult changePassword(String username, String oldPassword, String newPassword) {
        if (!ValidationUtil.isStrongPassword(newPassword)) {
            return ChangePasswordResult.INVALID_NEW_PASSWORD;
        }

        try {
            Optional<User> userOpt = userDAO.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ChangePasswordResult.USER_NOT_FOUND;
            }

            User user = userOpt.get();
            boolean oldMatches = PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash());
            if (!oldMatches) {
                auditDAO.log(username, "PASSWORD_CHANGE_ATTEMPT", "FAILURE");
                return ChangePasswordResult.WRONG_OLD_PASSWORD;
            }

            if (PasswordUtil.verifyPassword(newPassword, user.getPasswordHash())) {
                return ChangePasswordResult.SAME_AS_OLD_PASSWORD;
            }

            String newHash = PasswordUtil.hashPassword(newPassword);
            boolean updated = userDAO.updatePassword(user.getUserId(), newHash);

            if (updated) {
                auditDAO.log(username, "PASSWORD_CHANGE_SUCCESS", "SUCCESS");
                return ChangePasswordResult.SUCCESS;
            } else {
                auditDAO.log(username, "PASSWORD_CHANGE_ATTEMPT", "FAILURE");
                return ChangePasswordResult.DB_ERROR;
            }

        } catch (SQLException e) {
            System.err.println("Database error during password change: " + e.getMessage());
            return ChangePasswordResult.DB_ERROR;
        }
    }
}
