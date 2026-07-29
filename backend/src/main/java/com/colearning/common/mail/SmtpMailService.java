package com.colearning.common.mail;

import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * SMTP implementation of {@link MailService} using Spring's JavaMailSender.
 * Sends HTML emails for verification, password reset, and welcome messages.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpMailService implements MailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${spring.mail.username:noreply@colearning.local}")
    private String fromAddress;

    @Override
    public void sendVerificationEmail(String to, String verificationLink) {
        String subject = "[Co-Learning] 邮箱验证码";
        String html = """
            <div style="max-width:480px;margin:0 auto;font-family:sans-serif;">
              <h2 style="color:#2080F0;">欢迎加入 Co-Learning！</h2>
              <p>请使用以下验证码完成邮箱验证：</p>
              <p style="margin:24px 0;font-size:28px;font-weight:bold;letter-spacing:4px;color:#2080F0;">
                %s
              </p>
              <p style="color:#999;font-size:12px;">
                验证码有效期：24 小时<br>
                如果这不是您的操作，请忽略此邮件。
              </p>
            </div>
            """.formatted(verificationLink);
        sendHtml(to, subject, html);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        String subject = "[Co-Learning] 重置密码验证码";
        String html = """
            <div style="max-width:480px;margin:0 auto;font-family:sans-serif;">
              <h2 style="color:#2080F0;">重置密码</h2>
              <p>我们收到了您的密码重置请求。请使用以下验证码：</p>
              <p style="margin:24px 0;font-size:28px;font-weight:bold;letter-spacing:4px;color:#2080F0;">
                %s
              </p>
              <p style="color:#999;font-size:12px;">
                验证码有效期：1 小时<br>
                如果这不是您的操作，请忽略此邮件。
              </p>
            </div>
            """.formatted(resetLink);
        sendHtml(to, subject, html);
    }

    @Override
    public void sendWelcomeEmail(String to, String displayName) {
        String subject = "[Co-Learning] 欢迎加入伴学社区！";
        String html = """
            <div style="max-width:480px;margin:0 auto;font-family:sans-serif;">
              <h2 style="color:#2080F0;">你好，%s！</h2>
              <p>您的邮箱已验证成功，欢迎加入 Co-Learning 伴学社区！</p>
              <p>在这里，你可以：</p>
              <ul>
                <li>制定考试目标和每日学习计划</li>
                <li>使用专注计时器记录有效学习时长</li>
                <li>加入公开陪伴房，和同学一起学习</li>
                <li>通过学习获得经验，培养你的电子宠物</li>
                <li>参与周排行榜，追踪学习进度</li>
              </ul>
              <p>开始你的学习之旅吧！</p>
            </div>
            """.formatted(displayName);
        sendHtml(to, subject, html);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to: {} subject: {}", to, subject);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw BusinessException.of(ErrorCode.AUTH_EMAIL_SEND_FAILED);
        }
    }
}
