package com.example.earthtalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EarthTalkApplication {

    public static void main(String[] args) {
        SpringApplication.run(EarthTalkApplication.class, args);
    }

}
