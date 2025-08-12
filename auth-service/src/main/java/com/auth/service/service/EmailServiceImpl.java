package com.auth.service.service;


import com.auth.service.constants.CodeType;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;


    @Override
    public void sendEmail(String email, String code, CodeType codeType) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@example.com");
        message.setTo(email);
        message.setSubject(codeType.toString());
        message.setText("Hello,\n\nYour " + codeType.toString() + "  code is: " + code + "\n\nThank you!");
        mailSender.send(message);
    }

//    @Override
//    public void sendVerificationCode(String email, CodeType c) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("no-reply@example.com");
//        message.setTo(email);
//        message.setSubject(code);
//        message.setText("Hello,\n\nYour verification code is: " + code + "\n\nThank you!");
//        mailSender.send(message);
//    }
//
//    @Override
//    public void sendPasswordResetCode(String email, String resetCode, CodeType codeType) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("no-reply@example.com");
//        message.setTo(email);
//        message.setSubject(String.valueOf(codeType));
//        message.setText("Hello,\n\nYour verification code is: " + resetCode + "\n\nThank you!");
//        mailSender.send(message);
//    }
//
//    @Override
//    public void sendWelcomeEmail(String email, String firstName) {
//
//    }
//
//    @Override
//    public void sendPasswordResetConfirmation(String email, String firstName) {
//
//    }
//
//    @Override
//    public void sendPasswordResetEmail(String email, String resetCode, CodeType code) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("no-reply@example.com");
//        message.setTo(email);
//        message.setSubject(String.valueOf(code));
//        message.setText("Hello,\n\nYour Password reset code is: " + resetCode + "\n\nThank you!");
//        mailSender.send(message);
//    }

}