package com.auth.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        // MailHog SMTP settings
        mailSender.setHost("localhost");
        mailSender.setPort(1025);
        // No credentials needed for MailHog
        mailSender.setUsername(null);
        mailSender.setPassword(null);

        // Disable JavaMail session debugging (optional)
        Properties props = new Properties();
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "false");

        mailSender.setJavaMailProperties(props);

        return mailSender;
    }
}
