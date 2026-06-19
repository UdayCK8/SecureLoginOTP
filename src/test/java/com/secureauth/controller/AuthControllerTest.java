package com.secureauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureauth.dto.*;
import com.secureauth.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc tests for {@link AuthController}.
 * <p>
 * AuthService is mocked so these tests exercise the controller layer
 * (HTTP mapping, JSON serialization, session handling) without touching
 * the database or SMTP server.
 */
@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @BeforeEach
    void setUp() {
        reset(authService);
    }

    // ------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------

    @Test
    void register_success() throws Exception {
        when(authService.register("alice", "pass1234", "alice@example.com"))
                .thenReturn(AuthService.RegisterResult.SUCCESS);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("pass1234");
        req.setEmail("alice@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    void register_usernameTaken() throws Exception {
        when(authService.register(any(), any(), any()))
                .thenReturn(AuthService.RegisterResult.USERNAME_TAKEN);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bob\",\"password\":\"pass1234\",\"email\":\"bob@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username is already taken"));
    }

    // ------------------------------------------------------------------
    // Password check (step 1)
    // ------------------------------------------------------------------

    @Test
    void checkPassword_success_setsSession() throws Exception {
        when(authService.authenticatePassword("alice", "pass1234"))
                .thenReturn(AuthService.PasswordCheckResult.SUCCESS);

        LoginPasswordRequest req = new LoginPasswordRequest();
        req.setUsername("alice");
        req.setPassword("pass1234");

        mockMvc.perform(post("/api/auth/login/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(request().sessionAttribute("PENDING_USERNAME", "alice"));
    }

    @Test
    void checkPassword_wrongPassword() throws Exception {
        when(authService.authenticatePassword("alice", "wrong"))
                .thenReturn(AuthService.PasswordCheckResult.WRONG_PASSWORD);

        mockMvc.perform(post("/api/auth/login/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------
    // OTP issue (step 2a)
    // ------------------------------------------------------------------

    @Test
    void issueOtp_withoutSession_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login/otp/issue"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Password verification required"));
    }

    @Test
    void issueOtp_withSession_success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("PENDING_USERNAME", "alice");

        when(authService.issueOtp(eq("alice"), any()))
                .thenReturn(AuthService.OtpIssueResult.SENT);

        mockMvc.perform(post("/api/auth/login/otp/issue")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP sent to your email"));
    }

    // ------------------------------------------------------------------
    // OTP resend (step 2b)
    // ------------------------------------------------------------------

    @Test
    void resendOtp_withoutSession_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login/otp/resend"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Password verification required"));
    }

    @Test
    void resendOtp_success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("PENDING_USERNAME", "alice");

        when(authService.resendOtp(eq("alice"), any()))
                .thenReturn(AuthService.OtpIssueResult.SENT);

        mockMvc.perform(post("/api/auth/login/otp/resend")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New OTP sent to your email"));
    }

    // ------------------------------------------------------------------
    // OTP verify (step 3)
    // ------------------------------------------------------------------

    @Test
    void verifyOtp_withoutSession_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otp\":\"123456\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Password verification required"));
    }

    @Test
    void verifyOtp_success_setsAuthenticated() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("PENDING_USERNAME", "alice");

        when(authService.verifyOtp("alice", "123456"))
                .thenReturn(AuthService.OtpVerifyResult.VERIFIED);

        mockMvc.perform(post("/api/auth/login/otp/verify")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otp\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(request().sessionAttribute("AUTHENTICATED_USER", "alice"))
                .andExpect(request().sessionAttributeDoesNotExist("PENDING_USERNAME"));
    }

    @Test
    void verifyOtp_wrongOtp() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("PENDING_USERNAME", "alice");

        when(authService.verifyOtp("alice", "000000"))
                .thenReturn(AuthService.OtpVerifyResult.WRONG_OTP);

        mockMvc.perform(post("/api/auth/login/otp/verify")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otp\":\"000000\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid OTP"));
    }

    // ------------------------------------------------------------------
    // Change password
    // ------------------------------------------------------------------

    @Test
    void changePassword_success() throws Exception {
        when(authService.changePassword("alice", "oldPass1", "newPass1"))
                .thenReturn(AuthService.ChangePasswordResult.SUCCESS);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setUsername("alice");
        req.setOldPassword("oldPass1");
        req.setNewPassword("newPass1");

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    void changePassword_wrongOldPassword() throws Exception {
        when(authService.changePassword(any(), any(), any()))
                .thenReturn(AuthService.ChangePasswordResult.WRONG_OLD_PASSWORD);

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"oldPassword\":\"wrong\",\"newPassword\":\"newPass1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    // ------------------------------------------------------------------
    // Me
    // ------------------------------------------------------------------

    @Test
    void me_whenAuthenticated() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("AUTHENTICATED_USER", "alice");

        mockMvc.perform(get("/api/auth/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("alice"));
    }

    @Test
    void me_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated"));
    }

    // ------------------------------------------------------------------
    // Logout
    // ------------------------------------------------------------------

    @Test
    void logout_invalidatesSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("AUTHENTICATED_USER", "alice");

        mockMvc.perform(post("/api/auth/logout")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
