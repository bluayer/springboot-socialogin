package com.bluayer.springbootsocialogin.dto;

import com.bluayer.springbootsocialogin.domain.entity.SignupType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Optional;

@Getter
@NoArgsConstructor
public class UserSignUpRequest {
    @NotBlank(message = "이름을 작성해주세요")
    private String name;

    @NotBlank(message = "메일을 작성해주세요.")
    @Email(message = "메일의 양식을 지켜주세요.")
    private String email;

    @NotBlank(message = "액세스토큰을 작성해주세요")
    private String accessToken;

    private SignupType signupType;

    private Optional<String> code;

    @Builder
    public UserSignUpRequest(String name, String email, String accessToken, SignupType signupType, Optional<String> code) {
        this.name = name;
        this.email = email;
        this.accessToken = accessToken;
        this.signupType = signupType;
        this.code = code;
    }
}