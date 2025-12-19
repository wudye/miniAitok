package com.mwu.aitokstarter.email;

// java

import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
@Service
public class GmailMailService {

    private final JavaMailSender mailSender;
   // private final String defaultFrom;

    public GmailMailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
       // this.defaultFrom = defaultFrom;
    }

    public void sendSimpleEmail(String to, String subject, String text, String from) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        //msg.setFrom(from != null ? from : defaultFrom);
        mailSender.send(msg);
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody, String from,
                              String attachmentName, InputStreamSource attachmentSource) throws Exception {
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, attachmentSource != null, StandardCharsets.UTF_8.name());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
       // helper.setFrom(from != null ? from : defaultFrom);
        if (attachmentSource != null && attachmentName != null) {
            helper.addAttachment(attachmentName, attachmentSource);
        }
        mailSender.send(mime);
    }
}
