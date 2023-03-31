package com.griddynamics.pss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.griddynamics")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
