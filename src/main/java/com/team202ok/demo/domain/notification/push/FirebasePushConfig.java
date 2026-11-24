package com.team202ok.demo.domain.notification.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
@ConditionalOnProperty(name = "notification.push.provider", havingValue = "fcm")
class FirebasePushConfig {

    @Bean(destroyMethod = "delete")
    FirebaseApp notificationFirebaseApp(
            @Value("${notification.push.firebase.credentials-base64:}") String encodedCredentials
    ) throws IOException {
        GoogleCredentials credentials = encodedCredentials.isBlank()
                ? GoogleCredentials.getApplicationDefault()
                : ServiceAccountCredentials.fromStream(
                        new ByteArrayInputStream(Base64.getDecoder().decode(encodedCredentials)));
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setConnectTimeout(5000)
                .setReadTimeout(10000)
                .build();
        return FirebaseApp.initializeApp(options, "ssok-notifications");
    }

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp notificationFirebaseApp) {
        return FirebaseMessaging.getInstance(notificationFirebaseApp);
    }
}
