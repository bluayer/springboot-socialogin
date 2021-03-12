package com.bluayer.springbootsocialogin.domain.verifier;

import com.bluayer.springbootsocialogin.domain.vo.AppleKey;
import com.bluayer.springbootsocialogin.domain.vo.AppleKeys;
import com.bluayer.springbootsocialogin.dto.TokenResponse;
import com.bluayer.springbootsocialogin.dto.UserVerifiedResponse;
import com.bluayer.springbootsocialogin.dto.VerifyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.*;

@Slf4j
@Component
public class AppleVerifier implements UserAccessTokenVerifier {

    @Value("${apple.key.url}")
    private String APPLE_PUBLIC_KEYS_URL;

    @Value("${apple.aud}")
    private String AUD;

    @Value("${apple.sub}")
    private String SUB;

    @Value("${apple.team.id}")
    private String TEAM_ID;

    @Value("${apple.key.id}")
    private String KEY_ID;

    @Value("${apple.key.path}")
    private String KEY_PATH;

    @Value("${apple.auth.token.url}")
    private String AUTH_TOKEN_URL;

    private final RestTemplate appleVerifyClient = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserVerifiedResponse failResponse = UserVerifiedResponse.builder()
            .verified(false)
            .build();

    @Override
    public UserVerifiedResponse verify (VerifyRequest request) {
        try {
            String accessToken = request.getAccessToken();
            String code = request.getCode().orElseThrow(() -> new IllegalArgumentException("코드가 필요합니다."));
            boolean isVerified = verifyIdentityToken(accessToken);
            if (!isVerified) {
                return failResponse;
            }

            TokenResponse tokenResponse = validateAuthorizationGrantCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("코드가 유효하지 않습니다."));
            System.out.println(tokenResponse.toString());
            String idToken = tokenResponse.getId_token();
            SignedJWT signedJWT = SignedJWT.parse(idToken);
            ReadOnlyJWTClaimsSet payload = signedJWT.getJWTClaimsSet();
            String email = (String) payload.getClaim("email");
            boolean emailVerified = Boolean.parseBoolean((String)payload.getClaim("email_verified"));
            UserVerifiedResponse responseDto = UserVerifiedResponse.builder()
                    .email(email)
                    .verified(emailVerified)
                    .build();
            return responseDto;
        } catch (ParseException | IllegalArgumentException e) {
            return failResponse;
        }
    }

    private boolean verifyIdentityToken(String accessToken) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(accessToken);
        ReadOnlyJWTClaimsSet payload = signedJWT.getJWTClaimsSet();

        Date currentTime = new Date(System.currentTimeMillis());
        if (!currentTime.before(payload.getExpirationTime())) {
            return false;
        }

        if (!AUD.equals(payload.getIssuer()) || !SUB.equals(payload.getAudience().get(0))) {
            return false;
        }

        if (verifyPublicKey(signedJWT)) {
            return true;
        }
        return false;
    }

    private boolean verifyPublicKey(SignedJWT signedJWT) {
        try {
            AppleKeys appleKeys = appleVerifyClient.getForEntity(APPLE_PUBLIC_KEYS_URL, AppleKeys.class).getBody();
            if (appleKeys == null) {
                return false;
            }
            for (AppleKey appleKey : appleKeys.getKeys()) {
                RSAKey rsaKey = (RSAKey) JWK.parse(objectMapper.writeValueAsString(appleKey));
                RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
                JWSVerifier verifier = new RSASSAVerifier(publicKey);

                if (signedJWT.verify(verifier)) {
                    return true;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Optional<TokenResponse> validateAuthorizationGrantCode (String code) {
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        String clientSecret = createClientSecret();

        tokenRequest.add("client_id", SUB);
        tokenRequest.add("client_secret", clientSecret);
        tokenRequest.add("code", code);
        tokenRequest.add("grant_type", "authorization_code");

        return getTokenResponse(tokenRequest);
    }

    private String createClientSecret() {
        Date now = new Date();
        PrivateKey privateKey = getPrivateKey()
                .orElseThrow(() -> new IllegalArgumentException("Private key 읽기 실패"));

        return Jwts.builder()
                .setHeaderParam("kid", KEY_ID)
                .setHeaderParam("alg", "ES256")
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000))
                .setIssuer(TEAM_ID)
                .setAudience(AUD)
                .setSubject(SUB)
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }

    private Optional<PrivateKey> getPrivateKey() {
        try {
            ClassPathResource resource = new ClassPathResource(KEY_PATH);
            String privateKey = new String(Files.readAllBytes(Paths.get(resource.getURI())));
            Reader pemReader = new StringReader(privateKey);
            PEMParser pemParser = new PEMParser(pemReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
            return Optional.of(converter.getPrivateKey(object));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Optional<TokenResponse> getTokenResponse(MultiValueMap<String, String> tokenRequest) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(tokenRequest, headers);
            ResponseEntity<TokenResponse> responseEntity = appleVerifyClient.postForEntity(AUTH_TOKEN_URL, entity, TokenResponse.class);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
