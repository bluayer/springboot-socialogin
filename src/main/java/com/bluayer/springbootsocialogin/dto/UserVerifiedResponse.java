package com.bluayer.springbootsocialogin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserVerifiedResponse {
    private String email;
    private boolean verified;
    private String name;

    @Builder
    public UserVerifiedResponse(String email, boolean verified, String name) {
        this.email = email;
        this.verified = verified;
        this.name = name;
    }
}
