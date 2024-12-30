package com.example.controller;

import com.example.entity.EmailEvent;
import com.example.repository.EmailEventRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class TrackingController {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailEventRepository emailEventRepository;

    private static final Logger logger = LoggerFactory.getLogger(TrackingController.class);

    // Endpoint to send bulk emails
    @PostMapping("/sendBulkEmails")
    public void sendBulkEmails(@RequestBody List<String> recipientEmails) {
        String subject = "Your Subject Here";
        String body = "<html><body>" +
                "Email body content here. <br>" +
                "<img src='http://localhost:8080/track/open?email=%recipient%' style='display:none;' />" +
                "<br>" +
                "<a href='http://localhost:8080/track/click?email=%recipient%&url=https://destination.com'>Click here</a>" +
                "</body></html>";

        try {
            for (String recipientEmail : recipientEmails) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
                mimeMessageHelper.setTo(recipientEmail);
                mimeMessageHelper.setSubject(subject);
                mimeMessageHelper.setText(body.replace("%recipient%", recipientEmail), true); // Enable HTML content
                mailSender.send(mimeMessage);
                logger.info("HTML email sent to: " + recipientEmail);
            }
        } catch (Exception e) {
            logger.error("Error sending bulk emails", e);
        }
    }


    // Endpoint to track email opens (tracking pixel)
    @GetMapping("/track/open")
    public void trackEmailOpen(@RequestParam String email, HttpServletResponse response) throws IOException {
        // Log the email open event in MySQL
        EmailEvent emailEvent = new EmailEvent();
        emailEvent.setEmail(email);
        emailEvent.setEventType("OPEN");
        emailEvent.setEventTime(LocalDateTime.now());
        emailEventRepository.save(emailEvent);

        // Return a transparent 1x1 pixel image (empty response body)
        response.setContentType("image/png");
        response.getOutputStream().write(new byte[]{});  // Empty image
    }

    // Endpoint to track link clicks
    @GetMapping("/track/click")
    public void trackLinkClick(@RequestParam String email, @RequestParam String url, HttpServletResponse response) throws IOException {
        // Log the click event in MySQL
        EmailEvent emailEvent = new EmailEvent();
        emailEvent.setEmail(email);
        emailEvent.setEventType("CLICK");
        emailEvent.setUrl(url);
        emailEvent.setEventTime(LocalDateTime.now());
        emailEventRepository.save(emailEvent);

        // Redirect the user to the actual destination URL
        response.sendRedirect(url);
    }

    // Endpoint to count email opens
    @GetMapping("/count/opens")
    public long countEmailOpens(@RequestParam String email) {
        return emailEventRepository.countByEmailAndEventType(email, "OPEN");
    }

    // Endpoint to count link clicks
    @GetMapping("/count/clicks")
    public long countLinkClicks(@RequestParam String email, @RequestParam String url) {
        return emailEventRepository.countByEmailAndEventTypeAndUrl(email, "CLICK", url);
    }
}
