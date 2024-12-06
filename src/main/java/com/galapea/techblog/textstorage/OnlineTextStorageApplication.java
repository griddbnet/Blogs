package com.galapea.techblog.textstorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class OnlineTextStorageApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnlineTextStorageApplication.class, args);
    }
}
