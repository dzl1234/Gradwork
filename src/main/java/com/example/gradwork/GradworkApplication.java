package com.example.gradwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GradworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(GradworkApplication.class, args);
    }
}

