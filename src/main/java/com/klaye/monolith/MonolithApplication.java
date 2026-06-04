package com.klaye.monolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Classe principale de l'application Monolithe "All-in-One".
 * Regroupe l'Auth-Service, User-Service, Mission-Service, Dossier-Service et Document-Service.
 */
@SpringBootApplication
@EnableTransactionManagement
public class MonolithApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonolithApplication.class, args);
	}

}
