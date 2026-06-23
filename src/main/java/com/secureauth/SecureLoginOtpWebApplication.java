package com.secureauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class SecureLoginOtpWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureLoginOtpWebApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        String url = "http://localhost:8081/";

        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                Runtime.getRuntime().exec(
                        new String[]{"cmd", "/c", "start", url});
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(
                        new String[]{"open", url});
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec(
                        new String[]{"xdg-open", url});
            } else {
                System.out.println("Unsupported OS. Open manually: " + url);
            }

        } catch (Exception e) {
            System.err.println("Failed to open browser.");
            e.printStackTrace();
        }
    }
}