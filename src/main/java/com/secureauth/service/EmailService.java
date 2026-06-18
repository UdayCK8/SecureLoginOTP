package com.secureauth.service;

import com.secureauth.util.AppConfig;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Sends OTP emails via an SMTP server using the Jakarta Mail API.
 *
 * IMPORTANT (for the project report / viva):
 * Gmail no longer accepts your normal account password for SMTP.
 * You must:
 *   1. Enable 2-Step Verification on the Gmail account.
 *   2. Generate an "App Password" (Google Account -> Security ->
 *      App Passwords) and use that 16-character password here
 *      instead of your real password.
 *   3. Never commit that App Password to GitHub — load it from
 *      config.properties (gitignored) or an environment variable,
 *      as done via AppConfig in this project.
 */
public class EmailService {

    private final Session session;

    public EmailService() {
        AppConfig.validateMailConfig();

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", AppConfig.MAIL_SMTP_STARTTLS);
        props.put("mail.smtp.host", AppConfig.MAIL_SMTP_HOST);
        props.put("mail.smtp.port", AppConfig.MAIL_SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        AppConfig.MAIL_USERNAME,
                        AppConfig.MAIL_APP_PASSWORD
                );
            }
        });
    }

    /**
     * Sends a 6-digit OTP to the given recipient email address.
     *
     * @param toEmail   recipient's registered email
     * @param otp       the plain OTP value (only ever held in memory,
     *                  never persisted unhashed)
     * @param validMins how many minutes the OTP remains valid
     * @throws MessagingException if the SMTP send fails (auth error,
     *                            network issue, invalid recipient, etc.)
     */
    public void sendOtpEmail(String toEmail, String otp, int validMins) throws MessagingException {
        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(AppConfig.MAIL_USERNAME, "Secure Login System"));
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Failed to encode sender address", e);
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Your One-Time Password (OTP) for Secure Login");

        String body =
                "Hello,\n\n" +
                "Your One-Time Password (OTP) for logging into the Secure Login System is:\n\n" +
                "        " + otp + "\n\n" +
                "This OTP is valid for " + validMins + " minutes and can be used only once.\n" +
                "If you did not request this login, please ignore this email and consider " +
                "changing your password.\n\n" +
                "Regards,\n" +
                "Secure Login System (Academic Mini Project)";

        message.setText(body);
        Transport.send(message);
    }
}
