package com.example.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        // 1. Log current working directory for debugging
        System.out.println("Current Working Directory: " + System.getProperty("user.dir"));

        // 2. Load Dotenv (ignore if missing, useful for cloud deployment)
        Dotenv dotenv = Dotenv.configure()
                .directory("./") // Explicitly point to current directory
                .ignoreIfMissing()
                .load();

        // 3. Set System Properties with null safety checks and logging
        setSafeSystemProperty("DB_URL", dotenv.get("DB_URL"));
        setSafeSystemProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        setSafeSystemProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        setSafeSystemProperty("JPA_DDL_AUTO", dotenv.get("JPA_DDL_AUTO", "update"));
        setSafeSystemProperty("JPA_SHOW_SQL", dotenv.get("JPA_SHOW_SQL", "true"));
        setSafeSystemProperty("SERVER_PORT", dotenv.get("SERVER_PORT", "8080"));

        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * Set a system property safely. If the value is null, try to find it in system environment variables.
     * Prevents NullPointerException at System.setProperty if values are not provided in .env
     */
    private static void setSafeSystemProperty(String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            System.out.println("✅ Setting property from .env: " + key + " = " + (key.contains("PASSWORD") ? "******" : value));
            System.setProperty(key, value);
        } else {
            // Fallback to system environment variables (useful for cloud platforms like Railway)
            String envValue = System.getenv(key);
            if (envValue != null && !envValue.trim().isEmpty()) {
                System.out.println("🌐 Setting property from System Env: " + key + " = " + (key.contains("PASSWORD") ? "******" : envValue));
                System.setProperty(key, envValue);
            } else {
                System.err.println("⚠️ Warning: Configuration for '" + key + "' is missing in both .env and system environment.");
            }
        }
    }
}
