package com.bluayer.springbootsocialogin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Getter
@Builder
public class VerifyRequest {
    private final String accessToken;
    private final Optional<String> code;
}
