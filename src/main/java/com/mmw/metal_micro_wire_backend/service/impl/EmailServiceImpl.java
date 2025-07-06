package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.config.VerificationConfig;
import com.mmw.metal_micro_wire_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.Random;

/**
 * 邮件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final VerificationConfig verificationConfig;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Override
    public void sendVerificationCode(String to, String code, String type) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            
            String subject;
            String content;
            
            switch (type) {
                case "register":
                    subject = "【金属微丝】注册验证码";
                    content = createHtmlContent("注册验证码", code, "欢迎注册金属微丝系统！");
                    break;
                case "login":
                    subject = "【金属微丝】登录验证码";
                    content = createHtmlContent("登录验证码", code, "您正在登录金属微丝系统");
                    break;
                case "reset":
                    subject = "【金属微丝】重置密码验证码";
                    content = createHtmlContent("重置密码验证码", code, "您正在重置金属微丝系统密码");
                    break;
                default:
                    subject = "【金属微丝】验证码";
                    content = createHtmlContent("验证码", code, "金属微丝系统验证码");
            }
            
            helper.setSubject(subject);
            helper.setText(content, true); // true表示HTML格式
            
            mailSender.send(message);
            log.info("验证码邮件发送成功，收件人：{}，类型：{}", to, type);
            
        } catch (Exception e) {
            log.error("验证码邮件发送失败，收件人：{}，类型：{}，错误：{}", to, type, e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }
    
    /**
     * 创建HTML格式的邮件内容
     */
    private String createHtmlContent(String title, String code, String description) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                        body {
                            font-family: 'Microsoft YaHei', Arial, sans-serif;
                            background-color: #f5f5f5;
                            margin: 0;
                            padding: 20px;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background-color: #ffffff;
                            border-radius: 10px;
                            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                            overflow: hidden;
                        }
                        .header {
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            color: white;
                            padding: 30px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                            font-weight: normal;
                        }
                        .content {
                            padding: 40px 30px;
                            text-align: center;
                        }
                        .description {
                            color: #666;
                            font-size: 16px;
                            margin-bottom: 30px;
                            line-height: 1.5;
                        }
                        .code-container {
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            color: white;
                            padding: 20px;
                            border-radius: 8px;
                            margin: 20px 0;
                            display: inline-block;
                        }
                        .code {
                            font-size: 32px;
                            font-weight: bold;
                            letter-spacing: 8px;
                            margin: 0;
                            font-family: 'Courier New', monospace;
                        }
                        .notice {
                            color: #ff6b6b;
                            font-size: 14px;
                            margin-top: 30px;
                            padding: 15px;
                            background-color: #fff5f5;
                            border-left: 4px solid #ff6b6b;
                            border-radius: 4px;
                        }
                        .footer {
                            background-color: #f8f9fa;
                            padding: 20px;
                            text-align: center;
                            color: #888;
                            font-size: 12px;
                        }
                        .system-name {
                            color: #667eea;
                            font-weight: bold;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 %s</h1>
                        </div>
                        <div class="content">
                            <p class="description">%s</p>
                            <div class="code-container">
                                <div class="code">%s</div>
                            </div>
                            <div class="notice">
                                <strong>⚠️ 安全提醒</strong><br>
                                • 验证码有效期为 <strong>%s分钟</strong><br>
                                • 请勿将验证码泄露给他人<br>
                                • 如非本人操作，请忽略此邮件
                            </div>
                        </div>
                        <div class="footer">
                            <p>此邮件由 <span class="system-name">金属微丝系统</span> 自动发送，请勿回复</p>
                            <p>© 2025 金属微丝系统. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(title, title, description, code, verificationConfig.getCodeExpireMinutes());
    }
    
    @Override
    public String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false); // false表示纯文本格式

            mailSender.send(message);
            log.info("简单邮件发送成功，收件人：{}，主题：{}", to, subject);

        } catch (Exception e) {
            log.error("简单邮件发送失败，收件人：{}，主题：{}，错误：{}", to, subject, e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true表示HTML格式

            mailSender.send(message);
            log.info("HTML邮件发送成功，收件人：{}，主题：{}", to, subject);

        } catch (Exception e) {
            log.error("HTML邮件发送失败，收件人：{}，主题：{}，错误：{}", to, subject, e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}