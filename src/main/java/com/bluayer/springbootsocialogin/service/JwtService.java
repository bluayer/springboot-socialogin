package com.bluayer.springbootsocialogin.service;

import com.bluayer.springbootsocialogin.domain.vo.JwtPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;

@Service
public class JwtService {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${SECRET_KEY}")
    private String SECRET_KEY;

    public String createToken(JwtPayload payload) throws JsonProcessingException {
        SignatureAlgorithm  signatureAlgorithm= SignatureAlgorithm.HS256;
        byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());
        return Jwts.builder()
                .setSubject(objectMapper.writeValueAsString(payload))
                .signWith(signingKey, signatureAlgorithm)
                .compact();
    }

    public JwtPayload getPayload (String token) throws JsonProcessingException {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
                .build()
                .parseClaimsJws(token)
                .getBody();

        JwtPayload payload = objectMapper.readValue(claims.getSubject(), JwtPayload.class);
        return payload;
    }
}
