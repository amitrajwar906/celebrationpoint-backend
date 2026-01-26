package com.celebrationpoint.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.celebrationpoint.backend")
public class CelebrationpointBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CelebrationpointBackendApplication.class, args);
    }
}
