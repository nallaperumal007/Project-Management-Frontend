package com.zosh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.zosh.exception.MailsException;
import com.zosh.model.InviteToken;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private InviteTokenService inviteTokenService;

    public void sendEmailWithToken(String userEmail, String token) throws MessagingException, MailsException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        
         inviteTokenService.addToken(token,userEmail);
        String subject = "Join Project Team Invitation";
        String text = "Click the link to join the project team: http://localhost:3000?token=" + token;

        helper.setSubject(subject);
        helper.setText(text, true);
        helper.setTo(userEmail);

        try {
            javaMailSender.send(mimeMessage);
        } catch (MailException e) {
            throw new MailsException("Failed to send email");
        }
    }
}

