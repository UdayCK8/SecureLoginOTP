package com.secureauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *  make this open link: http://localhost:8081/
 * Spring Boot entry point for the Secure Login System web application.
 * <p>
 * This class launches an embedded Tomcat server serving the REST API
 * and static web frontend. The original console-based {@link com.secureauth.main.Main}
 * remains available and can still be run directly from an IDE.
 */
@SpringBootApplication
public class SecureLoginOtpWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureLoginOtpWebApplication.class, args);
    }
}
