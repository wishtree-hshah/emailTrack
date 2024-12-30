package com.example.service;

import com.example.repository.EmailEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailEventRepository emailEventRepository;

    public void sendBulkEmails(List<String> recipientEmails) {
        String subject = "Your Subject Here";
        String body = "<html><body>" +
                "Email body content here. <br>" +
                "<img src='http://localhost:8080/track/open?email=%recipient%' style='display:none;' />" +
                "<br>" +
                "<a href='http://localhost:8080/track/click?email=%recipient%&url=https://destination.com'>Click here</a>" +
                "</body></html>";

        for (String recipientEmail : recipientEmails) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body.replace("%recipient%", recipientEmail));
            mailSender.send(message);
            System.out.println("Email sent to: " + recipientEmail);
        }
    }
}
