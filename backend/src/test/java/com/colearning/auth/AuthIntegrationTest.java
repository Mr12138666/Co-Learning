package com.colearning.auth;

import com.colearning.BaseIntegrationTest;
import com.colearning.common.mail.MailService;
import com.colearning.common.storage.StorageService;
import com.colearning.auth.internal.repository.EmailVerificationRepository;
import com.colearning.auth.internal.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @MockBean
    private MailService mailService;

    @MockBean
    private StorageService storageService;

    private static final String TEST_EMAIL = "testuser@integration.test";
    private static final String TEST_PASSWORD = "Test1234!";
    private static final String TEST_DISPLAY_NAME = "测试用户";

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        emailVerificationRepository.deleteAll();
        userRepository.deleteAll();

        // Mock storage service to return a dummy avatar URL
        when(storageService.generateDefaultAvatar(anyString()))
                .thenReturn("https://api.dicebear.com/7.x/avataaars/svg?seed=test");
    }

    @Nested
    @DisplayName("注册流程")
    class RegisterTests {

        @Test
        @DisplayName("正常注册 → 返回成功 + 发送验证邮件")
        void register_success() throws Exception {
            Map<String, String> request = Map.of(
                    "email", TEST_EMAIL,
                    "password", TEST_PASSWORD,
                    "displayName", TEST_DISPLAY_NAME
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0"))
                    .andExpect(jsonPath("$.data").isEmpty());

            // Verify user was saved
            assertThat(userRepository.existsByEmail(TEST_EMAIL)).isTrue();

            // Verify verification email was sent
            verify(mailService).sendVerificationEmail(anyString(), anyString());
        }

        @Test
        @DisplayName("重复注册同一邮箱 → 返回错误")
        void register_duplicateEmail() throws Exception {
            // First registration
            registerUser(TEST_EMAIL, TEST_PASSWORD, TEST_DISPLAY_NAME);

            // Second registration with same email
            Map<String, String> request = Map.of(
                    "email", TEST_EMAIL,
                    "password", TEST_PASSWORD,
                    "displayName", "另一个用户"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isNotEmpty())
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        @DisplayName("密码少于8位 → 返回验证错误")
        void register_shortPassword() throws Exception {
            Map<String, String> request = Map.of(
                    "email", "short@test.com",
                    "password", "123",
                    "displayName", "短密码"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("邮箱格式错误 → 返回验证错误")
        void register_invalidEmail() throws Exception {
            Map<String, String> request = Map.of(
                    "email", "not-an-email",
                    "password", TEST_PASSWORD,
                    "displayName", "邮箱错误"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("邮箱验证流程")
    class VerifyEmailTests {

        @Test
        @DisplayName("使用有效令牌验证邮箱 → 成功")
        void verifyEmail_success() throws Exception {
            // Register and capture token
            String rawToken = registerAndCaptureToken(TEST_EMAIL, TEST_PASSWORD, TEST_DISPLAY_NAME);

            // Verify email
            Map<String, String> request = Map.of("token", rawToken);

            mockMvc.perform(post("/api/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0"));

            // Verify email_verified flag is set
            var user = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
            assertThat(user.getEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("使用无效令牌验证邮箱 → 返回错误")
        void verifyEmail_invalidToken() throws Exception {
            Map<String, String> request = Map.of("token", "invalid-token-12345");

            mockMvc.perform(post("/api/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isNotEmpty())
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("登录流程")
    class LoginTests {

        @Test
        @DisplayName("已验证用户登录 → 返回 accessToken + 设置 refresh cookie")
        void login_success() throws Exception {
            // Register and verify email
            String token = registerAndCaptureToken(TEST_EMAIL, TEST_PASSWORD, TEST_DISPLAY_NAME);
            verifyEmail(token);

            // Login
            Map<String, String> request = Map.of(
                    "email", TEST_EMAIL,
                    "password", TEST_PASSWORD
            );

            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0"))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.userId").isNumber())
                    .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.data.role").value("USER"))
                    .andExpect(jsonPath("$.data.emailVerified").value(true))
                    .andExpect(cookie().exists("refresh_token"))
                    .andReturn();

            // Verify access token is present in response
            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).contains("accessToken");
        }

        @Test
        @DisplayName("未验证邮箱用户登录 → 返回错误")
        void login_emailNotVerified() throws Exception {
            // Register but don't verify
            registerAndCaptureToken(TEST_EMAIL, TEST_PASSWORD, TEST_DISPLAY_NAME);

            Map<String, String> request = Map.of(
                    "email", TEST_EMAIL,
                    "password", TEST_PASSWORD
            );

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isNotEmpty())
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        @DisplayName("密码错误 → 返回认证失败")
        void login_wrongPassword() throws Exception {
            // Register and verify
            String token = registerAndCaptureToken(TEST_EMAIL, TEST_PASSWORD, TEST_DISPLAY_NAME);
            verifyEmail(token);

            Map<String, String> request = Map.of(
                    "email", TEST_EMAIL,
                    "password", "WrongPassword123"
            );

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isNotEmpty());
        }

        @Test
        @DisplayName("不存在的用户登录 → 返回认证失败")
        void login_userNotFound() throws Exception {
            Map<String, String> request = Map.of(
                    "email", "nonexistent@test.com",
                    "password", TEST_PASSWORD
            );

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("Token 刷新与登出流程")
    class TokenManagementTests {

        @Test
        @DisplayName("使用 refresh cookie 刷新 token → 返回新 accessToken")
        void refresh_success() throws Exception {
            // Register, verify, and login
            String token = registerAndCaptureToken(TEST_EMAIL, TEST_PASSWORD, TEST_DISPLAY_NAME);
            verifyEmail(token);
            String refreshToken = loginAndGetRefreshToken(TEST_EMAIL, TEST_PASSWORD);

            // Refresh
            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0"))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(cookie().exists("refresh_token"));
        }

        @Test
        @DisplayName("无 refresh cookie 刷新 → 返回错误")
        void refresh_noCookie() throws Exception {
            mockMvc.perform(post("/api/auth/refresh"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isNotEmpty());
        }

        @Test
        @DisplayName("登出 → 清除 refresh cookie")
        void logout_clearsCookie() throws Exception {
            // Register, verify, and login
            String token = registerAndCaptureToken(TEST_EMAIL, TEST_PASSWORD, TEST_DISPLAY_NAME);
            verifyEmail(token);
            String refreshToken = loginAndGetRefreshToken(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/logout")
                            .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0"))
                    .andExpect(cookie().maxAge("refresh_token", 0));

            // After logout, the old refresh token should no longer work
            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("忘记密码与重置密码流程")
    class PasswordResetTests {

        @Test
        @DisplayName("忘记密码 → 总是返回成功（不泄露邮箱是否存在）")
        void forgotPassword_alwaysSuccess() throws Exception {
            Map<String, String> request = Map.of("email", "nonexistent@test.com");

            mockMvc.perform(post("/api/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0"));
        }

        @Test
        @DisplayName("已注册用户忘记密码 → 发送重置邮件")
        void forgotPassword_existingUser() throws Exception {
            // Register user
            registerAndCaptureToken(TEST_EMAIL, TEST_PASSWORD, TEST_DISPLAY_NAME);

            Map<String, String> request = Map.of("email", TEST_EMAIL);

            mockMvc.perform(post("/api/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0"));

            // Verify password reset email was sent
            verify(mailService).sendPasswordResetEmail(anyString(), anyString());
        }

        @Test
        @DisplayName("使用有效重置令牌重置密码 → 成功")
        void resetPassword_success() throws Exception {
            // Register user
            registerAndCaptureToken(TEST_EMAIL, TEST_PASSWORD, TEST_DISPLAY_NAME);

            // Request password reset and capture the reset token
            ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
            mockMvc.perform(post("/api/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("email", TEST_EMAIL))))
                    .andExpect(status().isOk());
            verify(mailService).sendPasswordResetEmail(anyString(), tokenCaptor.capture());
            String resetToken = tokenCaptor.getValue();

            // Reset password
            String newPassword = "NewPassword123!";
            Map<String, String> request = Map.of(
                    "token", resetToken,
                    "newPassword", newPassword
            );

            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0"));

            // Login with new password (after verifying email first)
            var user = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
            user.setEmailVerified(true);
            userRepository.save(user);

            Map<String, String> loginRequest = Map.of(
                    "email", TEST_EMAIL,
                    "password", newPassword
            );

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0"))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
        }
    }

    // ===== Helper Methods =====

    private void registerUser(String email, String password, String displayName) throws Exception {
        Map<String, String> request = Map.of(
                "email", email,
                "password", password,
                "displayName", displayName
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private String registerAndCaptureToken(String email, String password, String displayName) throws Exception {
        registerUser(email, password, displayName);

        // Capture the verification token from the mock mailService
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendVerificationEmail(anyString(), tokenCaptor.capture());
        return tokenCaptor.getValue();
    }

    private void verifyEmail(String token) throws Exception {
        Map<String, String> request = Map.of("token", token);
        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private String loginAndGetRefreshToken(String email, String password) throws Exception {
        Map<String, String> request = Map.of("email", email, "password", password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        jakarta.servlet.http.Cookie[] cookies = result.getResponse().getCookies();
        assertThat(cookies).isNotNull();
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            if ("refresh_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        throw new AssertionError("refresh_token cookie not found in response");
    }
}
