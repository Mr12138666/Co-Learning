package com.colearning.auth;

import com.colearning.auth.dto.request.ForgotPasswordRequest;
import com.colearning.auth.dto.request.LoginRequest;
import com.colearning.auth.dto.request.RegisterRequest;
import com.colearning.auth.dto.request.ResetPasswordRequest;
import com.colearning.auth.dto.request.VerifyEmailRequest;
import com.colearning.auth.dto.response.TokenResponse;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authentication service: registration, login, token management, password reset.
 */
public interface AuthService {

    void register(RegisterRequest request);

    void resendVerificationEmail(String email);

    void verifyEmail(VerifyEmailRequest request);

    TokenResponse login(LoginRequest request, HttpServletResponse response);

    TokenResponse refresh(String refreshToken, HttpServletResponse response);

    void logout(String refreshToken);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
