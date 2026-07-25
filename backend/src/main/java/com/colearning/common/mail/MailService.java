package com.colearning.common.mail;

/**
 * Mail service interface for sending transactional emails.
 */
public interface MailService {

    void sendVerificationEmail(String to, String verificationLink);

    void sendPasswordResetEmail(String to, String resetLink);

    void sendWelcomeEmail(String to, String displayName);
}
