package com.treading_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/// Required depedency:

///     <dependency>
///       <groupId>org.springframework.boot</groupId>
///       <artifactId>spring-boot-starter-mail</artifactId>
///     </dependency>

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;


    public void sendVerificationOtpEmail(String userEmail, String otp)
            throws MessagingException, MailSendException
    {
        ///👉 Creates a new email message using Spring’s JavaMailSender.
        ///👉 MimeMessage supports complex emails (HTML, attachments, etc.).
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();


        ///👉MimeMessageHelper simplifies setting the subject, body, recipient, etc.
        /// Parameters:
        ///     mimeMessage: the message being built.
        ///     false: means this is a simple email (no attachments).
        ///     "UTF-8": character encoding to support all text properly.

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");



        ///👉 subject: The email subject line.
        String subject = "Account verification";

        ///👉 text: The body of the email.
        ///     Includes the OTP.
        ///     <b> is HTML for bold text (because we’ll use HTML in the email)
        String text = "Your account verification code is: <b>" + otp + "</b>";
        //String text = "your account verification code is : " + otp;


        ///👉 Sets the subject and body of the email.
        helper.setSubject(subject);
        helper.setText(text, true);

        ///👉 Specifies the recipient's email address
        helper.setTo(userEmail);


        try
        {
            ///👉 Sends the fully constructed email.
            javaMailSender.send(mimeMessage);

        }
        catch (MailException e)
        {
            throw new MailSendException("Failed to send email");
        }

    }
}
