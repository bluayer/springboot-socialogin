package com.bluayer.springbootsocialogin.domain.verifier;

import com.bluayer.springbootsocialogin.dto.KakaoVerifyResponse;
import com.bluayer.springbootsocialogin.dto.UserVerifiedResponse;
import com.bluayer.springbootsocialogin.dto.VerifyRequest;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@NoArgsConstructor
@Component
public class KakaoVerifier implements UserAccessTokenVerifier {
    private static final RestTemplate kakaoVerifyClient = new RestTemplate();
    private static final String url = "https://kapi.kakao.com/v2/user/me";
    private final UserVerifiedResponse failResponse = UserVerifiedResponse.builder()
            .verified(false)
            .build();
    private final Gson gson = new Gson();

    @Override
    public UserVerifiedResponse verify (VerifyRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(request.getAccessToken());
        String requestBody = "property_keys=[\"kakao_account.email\"]";
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = kakaoVerifyClient.postForEntity(url, httpEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            return failResponse;
        }
        KakaoVerifyResponse verifyResponse = gson.fromJson(response.getBody(), KakaoVerifyResponse.class);
        UserVerifiedResponse responseDto = UserVerifiedResponse.builder()
                .email(verifyResponse.getKakaoAccountData().getEmail())
                .verified(verifyResponse.getKakaoAccountData().isEmailVerified())
                .name(verifyResponse.getKakaoAccountData().getProfile().getNickname())
                .build();
        return responseDto;
    }
}

