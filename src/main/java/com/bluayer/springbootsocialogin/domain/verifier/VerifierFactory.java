package com.bluayer.springbootsocialogin.domain.verifier;

import com.bluayer.springbootsocialogin.domain.entity.SignupType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class VerifierFactory {

    private final GoogleVerifier googleVerifier;
    private final KakaoVerifier kakaoVerifier;
    private final AppleVerifier appleVerifier;

    public UserAccessTokenVerifier getVerifier (SignupType type) {
        switch (type) {
            case GOOGLE:
                return googleVerifier;
            case KAKAO:
                return kakaoVerifier;
            case APPLE:
                return appleVerifier;
            default:
                return null;
        }
    }
}
