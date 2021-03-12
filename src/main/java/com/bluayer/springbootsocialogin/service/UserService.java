package com.bluayer.springbootsocialogin.service;

import com.bluayer.springbootsocialogin.domain.entity.SignupType;
import com.bluayer.springbootsocialogin.domain.entity.User;
import com.bluayer.springbootsocialogin.domain.verifier.UserAccessTokenVerifier;
import com.bluayer.springbootsocialogin.domain.verifier.VerifierFactory;
import com.bluayer.springbootsocialogin.domain.vo.JwtPayload;
import com.bluayer.springbootsocialogin.dto.UserSignUpRequest;
import com.bluayer.springbootsocialogin.dto.UserVerifiedResponse;
import com.bluayer.springbootsocialogin.dto.VerifyRequest;
import com.bluayer.springbootsocialogin.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    private final JwtService jwtService;
    private final VerifierFactory verifierFactory;

    @Transactional
    public String socialLogin (UserSignUpRequest requestDto) throws JsonProcessingException {
        String accessToken = requestDto.getAccessToken();
        SignupType userType = requestDto.getSignupType();
        Optional<String> code = requestDto.getCode();

        UserAccessTokenVerifier verifier = verifierFactory.getVerifier(userType);
        if (verifier == null) {
            throw new IllegalArgumentException("잘못된 가입 타입입니다.");
        }

        VerifyRequest verifyRequest = VerifyRequest.builder()
                .accessToken(accessToken)
                .code(code)
                .build();

        UserVerifiedResponse responseDto = verifier.verify(verifyRequest);
        if (!responseDto.isVerified()) {
            throw new IllegalArgumentException("해당 토큰이 유효하지 않습니다.");
        }

        Optional<User> searchedUser = userRepository.findByEmail(requestDto.getEmail());
        if (searchedUser.isEmpty()) {
            User savedUser = userRepository.save(User.builder()
                    .email(requestDto.getEmail())
                    .name(requestDto.getName())
                    .accessToken(requestDto.getAccessToken())
                    .signupType(requestDto.getSignupType())
                    .build()
            );
            JwtPayload payload = new JwtPayload(savedUser.getId(), savedUser.getEmail());
            String token = jwtService.createToken(payload);
            return token;
        } else {
            User user = searchedUser.get();
            JwtPayload payload = new JwtPayload(user.getId(), user.getEmail());
            String token = jwtService.createToken(payload);
            return token;
        }
    }
}
