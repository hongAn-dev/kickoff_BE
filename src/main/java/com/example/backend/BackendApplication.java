package com.example.backend;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
						.ignoreIfMissing()
								.load();
		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		System.setProperty("JPA_DDL_AUTO", dotenv.get("JPA_DDL_AUTO", "update"));
		System.setProperty("JPA_SHOW_SQL", dotenv.get("JPA_SHOW_SQL", "true"));
		System.setProperty("SERVER_PORT", dotenv.get("SERVER_PORT", "8080"));

		SpringApplication.run(BackendApplication.class, args);
	}

}
