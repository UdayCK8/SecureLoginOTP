package com.secureauth.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central place for all configurable values: DB credentials and
 * SMTP credentials.
 *
 * Values are loaded from `config.properties` (placed on the
 * classpath, e.g. in src/main/resources). Environment variables
 * override file values if present, which is the recommended way
 * to supply secrets (so credentials are never committed to Git).
 *
 * Required keys in config.properties:
 *   db.host=localhost
 *   db.port=3306
 *   db.name=secure_login_otp
 *   db.user=root
 *   db.password=yourDbPassword
 *   mail.username=youraddress@gmail.com
 *   mail.app.password=your16charAppPassword
 *   mail.smtp.host=smtp.gmail.com
 *   mail.smtp.port=587
 *   mail.smtp.starttls.enable=true
 */
public class AppConfig {

    public static final String DB_HOST;
    public static final String DB_PORT;
    public static final String DB_NAME;
    public static final String DB_USER;
    public static final String DB_PASSWORD;

    public static final String MAIL_USERNAME;
    public static final String MAIL_APP_PASSWORD;
    public static final String MAIL_SMTP_HOST;
    public static final String MAIL_SMTP_PORT;
    public static final String MAIL_SMTP_STARTTLS;

    // OTP behaviour
    public static final int OTP_LENGTH = 6;
    public static final int OTP_VALID_MINUTES = 5;
    public static final int OTP_MAX_ATTEMPTS = 3;

    static {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("Warning: could not load config.properties — " +
                    "falling back to environment variables. " + e.getMessage());
        }

        DB_HOST = envOrProp("DB_HOST", props, "db.host", "localhost");
        DB_PORT = envOrProp("DB_PORT", props, "db.port", "3306");
        DB_NAME = envOrProp("DB_NAME", props, "db.name", "secure_login_otp");
        DB_USER = envOrProp("DB_USER", props, "db.user", "root");
        DB_PASSWORD = envOrProp("DB_PASSWORD", props, "db.password", "");

        MAIL_USERNAME = envOrProp("MAIL_USERNAME", props, "mail.username", "");
        MAIL_APP_PASSWORD = envOrProp("MAIL_APP_PASSWORD", props, "mail.app.password", "");
        MAIL_SMTP_HOST = envOrProp("MAIL_SMTP_HOST", props, "mail.smtp.host", "smtp.gmail.com");
        MAIL_SMTP_PORT = envOrProp("MAIL_SMTP_PORT", props, "mail.smtp.port", "587");
        MAIL_SMTP_STARTTLS = envOrProp("MAIL_SMTP_STARTTLS", props, "mail.smtp.starttls.enable", "true");
    }

    private AppConfig() {
    }

    private static String envOrProp(String envKey, Properties props, String propKey, String defaultVal) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) {
            return envVal;
        }
        return props.getProperty(propKey, defaultVal);
    }

    /**
     * Validates SMTP configuration and prints debug information.
     */
    public static void validateMailConfig() {

        System.out.println("======================================");
        System.out.println(" SMTP CONFIGURATION CHECK ");
        System.out.println("======================================");

        System.out.println("MAIL_USERNAME = [" + MAIL_USERNAME + "]");

        if (MAIL_APP_PASSWORD == null || MAIL_APP_PASSWORD.isBlank()) {
            System.out.println("MAIL_APP_PASSWORD = [MISSING]");
        } else {
            System.out.println("MAIL_APP_PASSWORD = [FOUND]");
        }

        System.out.println("MAIL_SMTP_HOST = [" + MAIL_SMTP_HOST + "]");
        System.out.println("MAIL_SMTP_PORT = [" + MAIL_SMTP_PORT + "]");
        System.out.println("======================================");

        if (MAIL_USERNAME == null ||
            MAIL_USERNAME.isBlank() ||
            MAIL_USERNAME.equalsIgnoreCase("youraddress@gmail.com")) {

            throw new IllegalStateException(
                    "SMTP username is missing.\n" +
                    "Please set mail.username in config.properties");
        }

        if (MAIL_APP_PASSWORD == null ||
            MAIL_APP_PASSWORD.isBlank() ||
            MAIL_APP_PASSWORD.equalsIgnoreCase("YOUR_16_CHAR_APP_PASSWORD")) {

            throw new IllegalStateException(
                    "SMTP App Password is missing.\n" +
                    "Please set mail.app.password in config.properties");
        }
    }
}
