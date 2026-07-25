package com.colearning.auth.internal;

import com.colearning.auth.AuthService;
import com.colearning.auth.dto.response.TokenResponse;
import com.colearning.common.security.PrincipalUser;
import com.colearning.common.config.AppProperties;
import com.colearning.common.security.JwtTokenService;
import com.colearning.common.security.Argon2PasswordEncoder;
import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.common.mail.MailService;
import com.colearning.common.storage.StorageService;
import com.colearning.auth.dto.request.ForgotPasswordRequest;
import com.colearning.auth.dto.request.LoginRequest;
import com.colearning.auth.dto.request.RegisterRequest;
import com.colearning.auth.dto.request.ResetPasswordRequest;
import com.colearning.auth.dto.request.VerifyEmailRequest;
import com.colearning.auth.internal.entity.EmailVerification;
import com.colearning.auth.internal.entity.User;
import com.colearning.auth.internal.repository.EmailVerificationRepository;
import com.colearning.auth.internal.repository.UserRepository;
import com.colearning.user.internal.entity.UserProfile;
import com.colearning.user.internal.repository.UserProfileRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link AuthService}.
 * Handles registration, email verification, login, token refresh, logout, password reset.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserProfileRepository userProfileRepository;
    private final Argon2PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final MailService mailService;
    private final StorageService storageService;
    private final AppProperties appProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final int VERIFICATION_TOKEN_HOURS = 24;
    private static final int RESET_TOKEN_MINUTES = 60;
    private static final int MAX_FAILED_LOGIN = 5;
    private static final int LOCK_MINUTES = 15;
    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    // ===== Registration =====

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw BusinessException.of(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }

        // Create user
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .emailVerified(false)
                .status("ACTIVE")
                .role("USER")
                .build();
        user = userRepository.save(user);

        // Create profile with default avatar
        String defaultAvatar = storageService.generateDefaultAvatar(user.getEmail());
        UserProfile profile = UserProfile.builder()
                .userId(user.getId())
                .displayName(request.displayName())
                .avatarUrl(defaultAvatar)
                .build();
        userProfileRepository.save(profile);

        // Generate and send verification email
        String rawToken = generateAndStoreVerificationToken(user.getId(), "REGISTER");
        mailService.sendVerificationEmail(user.getEmail(), rawToken);

        log.info("User registered: email={}", user.getEmail());
    }

    // ===== Email Verification =====

    @Override
    public void verifyEmail(VerifyEmailRequest request) {
        String tokenHash = hashToken(request.token());
        EmailVerification verification = emailVerificationRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> BusinessException.of(ErrorCode.AUTH_VERIFICATION_TOKEN_INVALID));

        if (verification.isUsed()) {
            throw BusinessException.of(ErrorCode.AUTH_VERIFICATION_TOKEN_INVALID);
        }
        if (verification.isExpired()) {
            throw BusinessException.of(ErrorCode.AUTH_VERIFICATION_TOKEN_INVALID);
        }
        if (!"REGISTER".equals(verification.getPurpose())) {
            throw BusinessException.of(ErrorCode.AUTH_VERIFICATION_TOKEN_INVALID);
        }

        // Mark email as verified
        User user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND));
        user.setEmailVerified(true);
        userRepository.save(user);

        // Mark token as used
        verification.setUsedAt(Instant.now());
        emailVerificationRepository.save(verification);

        // Send welcome email
        UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);
        String displayName = profile != null ? profile.getDisplayName() : user.getEmail();
        mailService.sendWelcomeEmail(user.getEmail(), displayName);

        log.info("Email verified: userId={}", user.getId());
    }

    // ===== Login =====

    @Override
    public TokenResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> BusinessException.of(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (user.isLocked()) {
            throw BusinessException.of(ErrorCode.AUTH_ACCOUNT_LOCKED);
        }
        if (!user.isActive()) {
            throw BusinessException.of(ErrorCode.AUTH_ACCOUNT_SUSPENDED);
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            // Increment failed login count
            int newCount = user.getFailedLoginCount() + 1;
            user.setFailedLoginCount(newCount);
            if (newCount >= MAX_FAILED_LOGIN) {
                user.setLockedUntil(Instant.now().plus(Duration.ofMinutes(LOCK_MINUTES)));
                log.warn("Account locked due to failed logins: email={}", user.getEmail());
            }
            userRepository.save(user);
            throw BusinessException.of(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        // Reset failed login, update last login
        userRepository.resetFailedLogin(user.getId(), Instant.now());

        if (!user.getEmailVerified()) {
            throw BusinessException.of(ErrorCode.AUTH_EMAIL_NOT_VERIFIED);
        }

        PrincipalUser principal = toPrincipal(user);
        String accessToken = jwtTokenService.generateAccessToken(principal);
        String refreshToken = jwtTokenService.generateRefreshToken(principal);

        setRefreshTokenCookie(response, refreshToken);

        log.info("User logged in: email={}", user.getEmail());
        return buildTokenResponse(accessToken, principal);
    }

    // ===== Token Refresh =====

    @Override
    public TokenResponse refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw BusinessException.of(ErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        PrincipalUser principal = jwtTokenService.validateRefreshToken(refreshToken);
        String newRefreshToken = jwtTokenService.rotateRefreshToken(refreshToken, principal);
        String newAccessToken = jwtTokenService.generateAccessToken(principal);

        setRefreshTokenCookie(response, newRefreshToken);
        return buildTokenResponse(newAccessToken, principal);
    }

    // ===== Logout =====

    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            jwtTokenService.revokeRefreshToken(refreshToken);
            log.info("Refresh token revoked");
        }
    }

    // ===== Forgot Password =====

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always return success (don't leak whether email exists)
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String rawToken = generateAndStoreVerificationToken(user.getId(), "PASSWORD_RESET");
            mailService.sendPasswordResetEmail(user.getEmail(), rawToken);
            log.info("Password reset email sent: email={}", user.getEmail());
        });
    }

    // ===== Reset Password =====

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashToken(request.token());
        EmailVerification verification = emailVerificationRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> BusinessException.of(ErrorCode.AUTH_VERIFICATION_TOKEN_INVALID));

        if (verification.isUsed() || verification.isExpired()) {
            throw BusinessException.of(ErrorCode.AUTH_VERIFICATION_TOKEN_INVALID);
        }
        if (!"PASSWORD_RESET".equals(verification.getPurpose())) {
            throw BusinessException.of(ErrorCode.AUTH_VERIFICATION_TOKEN_INVALID);
        }

        User user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        verification.setUsedAt(Instant.now());
        emailVerificationRepository.save(verification);

        log.info("Password reset: userId={}", user.getId());
    }

    // ===== Helper Methods =====

    private String generateAndStoreVerificationToken(Long userId, String purpose) {
        // Generate a random token (URL-safe base64)
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String rawToken = HexFormat.of().formatHex(bytes);

        // Store the hash
        String tokenHash = hashToken(rawToken);
        Instant expiresAt = "REGISTER".equals(purpose)
                ? Instant.now().plus(Duration.ofHours(VERIFICATION_TOKEN_HOURS))
                : Instant.now().plus(Duration.ofMinutes(RESET_TOKEN_MINUTES));

        EmailVerification verification = EmailVerification.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .purpose(purpose)
                .expiresAt(expiresAt)
                .createdAt(Instant.now())
                .build();
        emailVerificationRepository.save(verification);

        return rawToken;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private PrincipalUser toPrincipal(User user) {
        return new PrincipalUser(user.getId(), user.getEmail(), user.getRole(), user.getEmailVerified());
    }

    private TokenResponse buildTokenResponse(String accessToken, PrincipalUser principal) {
        long expiresIn = appProperties.getJwt().getAccessTokenTtl();
        Instant expiresAt = Instant.now().plusSeconds(expiresIn);
        return new TokenResponse(
                accessToken,
                expiresIn,
                expiresAt,
                principal.userId(),
                principal.email(),
                principal.role(),
                principal.emailVerified()
        );
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);  // Set to true in production (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge((int) appProperties.getJwt().getRefreshTokenTtl());
        response.addCookie(cookie);
    }
}
