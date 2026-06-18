package com.secureauth.service;

import com.secureauth.dao.LoginAuditDAO;
import com.secureauth.dao.OtpDAO;
import com.secureauth.dao.UserDAO;
import com.secureauth.model.OtpRecord;
import com.secureauth.model.User;
import com.secureauth.util.AppConfig;
import com.secureauth.util.PasswordUtil;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private OtpDAO otpDAO;

    @Mock
    private LoginAuditDAO auditDAO;

    @Mock
    private EmailService emailService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userDAO, otpDAO, auditDAO, emailService);
    }

    private User mockUser(String username, String passwordHash, String email) {
        User user = new User();
        user.setUserId(1);
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setPasswordSalt("");
        user.setEmail(email);
        user.setActive(true);
        return user;
    }

    // ---------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------

    @Test
    void registerSuccess() throws SQLException {
        when(userDAO.usernameExists("newuser")).thenReturn(false);
        when(userDAO.emailExists("new@example.com")).thenReturn(false);
        when(userDAO.insertUser(any(User.class))).thenReturn(42);

        AuthService.RegisterResult result = authService.register("newuser", "Pass1234", "new@example.com");

        assertEquals(AuthService.RegisterResult.SUCCESS, result);
        verify(userDAO).insertUser(argThat(u ->
            u.getUsername().equals("newuser") &&
            u.getEmail().equals("new@example.com") &&
            PasswordUtil.verifyPassword("Pass1234", u.getPasswordHash())
        ));
    }

    @Test
    void registerUsernameTaken() throws SQLException {
        when(userDAO.usernameExists("existing")).thenReturn(true);

        AuthService.RegisterResult result = authService.register("existing", "Pass1234", "ex@example.com");

        assertEquals(AuthService.RegisterResult.USERNAME_TAKEN, result);
        verify(userDAO, never()).insertUser(any());
    }

    @Test
    void registerEmailTaken() throws SQLException {
        when(userDAO.usernameExists("newuser")).thenReturn(false);
        when(userDAO.emailExists("taken@example.com")).thenReturn(true);

        AuthService.RegisterResult result = authService.register("newuser", "Pass1234", "taken@example.com");

        assertEquals(AuthService.RegisterResult.EMAIL_TAKEN, result);
    }

    @Test
    void registerInvalidInputs() {
        assertEquals(AuthService.RegisterResult.INVALID_USERNAME, authService.register("ab", "Pass1234", "a@b.com"));
        assertEquals(AuthService.RegisterResult.INVALID_PASSWORD, authService.register("validuser", "short", "a@b.com"));
        assertEquals(AuthService.RegisterResult.INVALID_EMAIL, authService.register("validuser", "Pass1234", "bad-email"));
    }

    @Test
    void registerDbError() throws SQLException {
        when(userDAO.usernameExists("newuser")).thenThrow(new SQLException("db down"));

        AuthService.RegisterResult result = authService.register("newuser", "Pass1234", "new@example.com");

        assertEquals(AuthService.RegisterResult.DB_ERROR, result);
    }

    // ---------------------------------------------------------------
    // Password Authentication
    // ---------------------------------------------------------------

    @Test
    void authenticatePasswordSuccess() throws SQLException {
        String hash = PasswordUtil.hashPassword("correct");
        User user = mockUser("alice", hash, "alice@example.com");
        when(userDAO.findByUsername("alice")).thenReturn(Optional.of(user));

        AuthService.PasswordCheckResult result = authService.authenticatePassword("alice", "correct");

        assertEquals(AuthService.PasswordCheckResult.SUCCESS, result);
        verify(auditDAO).log("alice", "PASSWORD_CHECK", "SUCCESS");
    }

    @Test
    void authenticatePasswordWrongPassword() throws SQLException {
        String hash = PasswordUtil.hashPassword("correct");
        User user = mockUser("alice", hash, "alice@example.com");
        when(userDAO.findByUsername("alice")).thenReturn(Optional.of(user));

        AuthService.PasswordCheckResult result = authService.authenticatePassword("alice", "wrong");

        assertEquals(AuthService.PasswordCheckResult.WRONG_PASSWORD, result);
        verify(auditDAO).log("alice", "PASSWORD_CHECK", "FAILURE");
    }

    @Test
    void authenticatePasswordUserNotFound() throws SQLException {
        when(userDAO.findByUsername("nobody")).thenReturn(Optional.empty());

        AuthService.PasswordCheckResult result = authService.authenticatePassword("nobody", "any");

        assertEquals(AuthService.PasswordCheckResult.USER_NOT_FOUND, result);
    }

    @Test
    void authenticatePasswordDbError() throws SQLException {
        when(userDAO.findByUsername("alice")).thenThrow(new SQLException("db down"));

        AuthService.PasswordCheckResult result = authService.authenticatePassword("alice", "pass");

        assertEquals(AuthService.PasswordCheckResult.DB_ERROR, result);
    }

    // ---------------------------------------------------------------
    // OTP Issuance
    // ---------------------------------------------------------------

    @Test
    void issueOtpSuccess() throws SQLException, MessagingException {
        User user = mockUser("alice", "hash", "alice@example.com");
        when(userDAO.findByUsername("alice")).thenReturn(Optional.of(user));
        when(otpDAO.insertOtp(eq(1), anyString(), any(LocalDateTime.class))).thenReturn(99);

        AtomicInteger holder = new AtomicInteger();
        AuthService.OtpIssueResult result = authService.issueOtp("alice", holder);

        assertEquals(AuthService.OtpIssueResult.SENT, result);
        assertEquals(99, holder.get());
        verify(otpDAO).deleteExpiredOtps();
        verify(emailService).sendOtpEmail(eq("alice@example.com"), anyString(), eq(AppConfig.OTP_VALID_MINUTES));
        verify(auditDAO).log("alice", "OTP_SENT", "SUCCESS");
    }

    @Test
    void issueOtpUserNotFound() throws SQLException {
        when(userDAO.findByUsername("nobody")).thenReturn(Optional.empty());

        AuthService.OtpIssueResult result = authService.issueOtp("nobody", new AtomicInteger());

        assertEquals(AuthService.OtpIssueResult.USER_NOT_FOUND, result);
    }

    @Test
    void issueOtpEmailFailed() throws SQLException, MessagingException {
        User user = mockUser("alice", "hash", "alice@example.com");
        when(userDAO.findByUsername("alice")).thenReturn(Optional.of(user));
        when(otpDAO.insertOtp(eq(1), anyString(), any(LocalDateTime.class))).thenReturn(99);
        doThrow(new MessagingException("SMTP failed"))
            .when(emailService).sendOtpEmail(anyString(), anyString(), anyInt());

        AuthService.OtpIssueResult result = authService.issueOtp("alice", new AtomicInteger());

        assertEquals(AuthService.OtpIssueResult.EMAIL_FAILED, result);
        verify(auditDAO).log("alice", "OTP_SENT", "FAILURE");
    }

    // ---------------------------------------------------------------
    // OTP Verification
    // ---------------------------------------------------------------

    @Test
    void verifyOtpCorrect() throws SQLException, MessagingException {
        AtomicInteger holder = new AtomicInteger();
        setupActiveOtp("alice", holder);
        String otp = captureOtpValue("alice");
        String otpHash = captureOtpHash("alice");
        setupOtpRecordForVerification(1, otpHash);

        AuthService.OtpVerifyResult result = authService.verifyOtp("alice", otp);

        assertEquals(AuthService.OtpVerifyResult.VERIFIED, result);
        verify(otpDAO).markUsed(anyInt());
        verify(auditDAO).log("alice", "OTP_VERIFIED", "SUCCESS");
    }

    @Test
    void verifyOtpWrong() throws SQLException, MessagingException {
        AtomicInteger holder = new AtomicInteger();
        setupActiveOtp("alice", holder);
        String otpHash = captureOtpHash("alice");
        setupOtpRecordForVerification(1, otpHash);

        AuthService.OtpVerifyResult result = authService.verifyOtp("alice", "999999");

        assertEquals(AuthService.OtpVerifyResult.WRONG_OTP, result);
        verify(otpDAO, times(1)).incrementAttempt(anyInt());
        verify(auditDAO).log("alice", "OTP_VERIFIED", "FAILURE");
    }

    @Test
    void verifyOtpInvalidFormatCountsAsAttempt() throws SQLException, MessagingException {
        AtomicInteger holder = new AtomicInteger();
        setupActiveOtp("alice", holder);
        String otpHash = captureOtpHash("alice");
        setupOtpRecordForVerification(1, otpHash);

        AuthService.OtpVerifyResult result = authService.verifyOtp("alice", "abc");

        assertEquals(AuthService.OtpVerifyResult.WRONG_OTP, result);
        verify(otpDAO, times(1)).incrementAttempt(anyInt());
    }

    @Test
    void verifyOtpMaxAttemptsExceeded() throws SQLException {
        User user = mockUser("alice", "hash", "alice@example.com");
        when(userDAO.findByUsername("alice")).thenReturn(Optional.of(user));

        OtpRecord record = new OtpRecord();
        record.setOtpId(42);
        record.setUserId(1);
        record.setOtpCodeHash("somehash");
        record.setAttemptCount(AppConfig.OTP_MAX_ATTEMPTS);
        record.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        record.setUsed(false);
        when(otpDAO.findLatestActiveOtp(1)).thenReturn(Optional.of(record));

        AuthService.OtpVerifyResult result = authService.verifyOtp("alice", "123456");

        assertEquals(AuthService.OtpVerifyResult.MAX_ATTEMPTS_EXCEEDED, result);
        verify(otpDAO, never()).incrementAttempt(anyInt());
    }

    @Test
    void verifyOtpNoActiveOtp() throws SQLException {
        User user = mockUser("alice", "hash", "alice@example.com");
        when(userDAO.findByUsername("alice")).thenReturn(Optional.of(user));
        when(otpDAO.findLatestActiveOtp(1)).thenReturn(Optional.empty());

        AuthService.OtpVerifyResult result = authService.verifyOtp("alice", "123456");

        assertEquals(AuthService.OtpVerifyResult.NO_ACTIVE_OTP, result);
    }

    // ---------------------------------------------------------------
    // Change Password
    // ---------------------------------------------------------------

    @Test
    void changePasswordSuccess() throws SQLException {
        String oldHash = PasswordUtil.hashPassword("OldPass1");
        User user = mockUser("alice", oldHash, "alice@example.com");
        when(userDAO.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userDAO.updatePassword(eq(1), anyString())).thenReturn(true);

        AuthService.ChangePasswordResult result = authService.changePassword("alice", "OldPass1", "NewPass2");

        assertEquals(AuthService.ChangePasswordResult.SUCCESS, result);
        verify(userDAO).updatePassword(eq(1), argThat(hash -> PasswordUtil.verifyPassword("NewPass2", hash)));
        verify(auditDAO).log("alice", "PASSWORD_CHANGE_SUCCESS", "SUCCESS");
    }

    @Test
    void changePasswordWrongOldPassword() throws SQLException {
        String oldHash = PasswordUtil.hashPassword("OldPass1");
        User user = mockUser("alice", oldHash, "alice@example.com");
        when(userDAO.findByUsername("alice")).thenReturn(Optional.of(user));

        AuthService.ChangePasswordResult result = authService.changePassword("alice", "WrongOld", "NewPass2");

        assertEquals(AuthService.ChangePasswordResult.WRONG_OLD_PASSWORD, result);
    }

    @Test
    void changePasswordSameAsOld() throws SQLException {
        String oldHash = PasswordUtil.hashPassword("SamePass1");
        User user = mockUser("alice", oldHash, "alice@example.com");
        when(userDAO.findByUsername("alice")).thenReturn(Optional.of(user));

        AuthService.ChangePasswordResult result = authService.changePassword("alice", "SamePass1", "SamePass1");

        assertEquals(AuthService.ChangePasswordResult.SAME_AS_OLD_PASSWORD, result);
    }

    @Test
    void changePasswordWeakNewPassword() {
        AuthService.ChangePasswordResult result = authService.changePassword("alice", "old", "weak");
        assertEquals(AuthService.ChangePasswordResult.INVALID_NEW_PASSWORD, result);
    }

    // ---------------------------------------------------------------
    // Resend OTP
    // ---------------------------------------------------------------

    @Test
    void resendOtpMarksPreviousUsedAndIssuesNew() throws SQLException, MessagingException {
        User user = mockUser("alice", "hash", "alice@example.com");
        when(userDAO.findByUsername("alice")).thenReturn(Optional.of(user));
        when(otpDAO.insertOtp(eq(1), anyString(), any(LocalDateTime.class))).thenReturn(88);

        AtomicInteger holder = new AtomicInteger();
        AuthService.OtpIssueResult result = authService.resendOtp("alice", holder);

        assertEquals(AuthService.OtpIssueResult.SENT, result);
        verify(otpDAO).markAllUsedForUser(1);
        verify(auditDAO).log("alice", "OTP_RESENT", "SUCCESS");
        verify(otpDAO).deleteExpiredOtps();
        verify(emailService).sendOtpEmail(eq("alice@example.com"), anyString(), eq(AppConfig.OTP_VALID_MINUTES));
    }

    // ---------------------------------------------------------------
    // Helper methods for OTP capture
    // ---------------------------------------------------------------

    private void setupActiveOtp(String username, AtomicInteger holder) throws SQLException, MessagingException {
        User user = mockUser(username, "hash", username + "@example.com");
        when(userDAO.findByUsername(username)).thenReturn(Optional.of(user));
        when(otpDAO.insertOtp(eq(1), anyString(), any(LocalDateTime.class))).thenReturn(99);
        authService.issueOtp(username, holder);
    }

    private String captureOtpValue(String username) throws MessagingException {
        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendOtpEmail(eq(username + "@example.com"), otpCaptor.capture(), eq(AppConfig.OTP_VALID_MINUTES));
        return otpCaptor.getValue();
    }

    private String captureOtpHash(String username) throws SQLException {
        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(otpDAO).insertOtp(eq(1), hashCaptor.capture(), any(LocalDateTime.class));
        return hashCaptor.getValue();
    }

    private void setupOtpRecordForVerification(int userId, String otpHash) throws SQLException {
        OtpRecord record = new OtpRecord();
        record.setOtpId(99);
        record.setUserId(userId);
        record.setOtpCodeHash(otpHash);
        record.setAttemptCount(0);
        record.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        record.setUsed(false);
        when(otpDAO.findLatestActiveOtp(userId)).thenReturn(Optional.of(record));
    }
}
