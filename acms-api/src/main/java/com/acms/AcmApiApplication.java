package com.acms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the Agent Commission Management System (ACMS) API.
 * <p>
 * This class initializes the Spring Boot application and enables essential features
 * including JPA Auditing and Spring Caching.
 * </p>
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class AcmApiApplication {

    /**
     * Main method to start the ACMS API application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AcmApiApplication.class, args);
    }
}
