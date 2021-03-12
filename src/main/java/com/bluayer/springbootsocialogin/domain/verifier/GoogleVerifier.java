package com.bluayer.springbootsocialogin.domain.verifier;

import com.bluayer.springbootsocialogin.dto.UserVerifiedResponse;
import com.bluayer.springbootsocialogin.dto.VerifyRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@Slf4j
@Component
public class GoogleVerifier implements UserAccessTokenVerifier {

    private static final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private static final HttpTransport transport = new NetHttpTransport();
    private final UserVerifiedResponse failResponse = UserVerifiedResponse.builder()
            .verified(false)
            .build();

    @Value("${google.ios.client}")
    private String IOS_CLIENT_ID;

    @Value("${google.android.client}")
    private String ANDROID_CLIENT_ID;

    @Override
    public UserVerifiedResponse verify (VerifyRequest request) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Arrays.asList(IOS_CLIENT_ID, ANDROID_CLIENT_ID))
                .build();
        try {
            GoogleIdToken idToken = verifier.verify(request.getAccessToken());
            if (idToken != null) {
                Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                boolean emailVerified = payload.getEmailVerified();
                String name = (String) payload.get("name");

                UserVerifiedResponse responseDto = UserVerifiedResponse.builder()
                        .email(email)
                        .verified(emailVerified)
                        .name(name)
                        .build();
                return responseDto;
            }
            return failResponse;
        } catch (GeneralSecurityException | IOException e) {
            log.info(e.getMessage());
            return failResponse;
        }

    }

}
