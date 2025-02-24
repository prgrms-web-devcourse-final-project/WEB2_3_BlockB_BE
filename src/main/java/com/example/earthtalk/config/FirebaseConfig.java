package com.example.earthtalk.config;

import com.example.earthtalk.global.exception.BadRequestException;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/firebase.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if(FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (FileNotFoundException e) {
            throw new NotFoundException(ErrorCode.NOT_FOUND);
        } catch (IOException e) {
            throw new BadRequestException(ErrorCode.INVALID_REQUEST_BODY);
        }
    }
}
