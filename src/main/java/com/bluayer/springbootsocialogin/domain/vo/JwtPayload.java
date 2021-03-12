package com.bluayer.springbootsocialogin.domain.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JwtPayload {
    private long id;
    private String email;

    public JwtPayload(Long id, String email) {
        this.id = id;
        this.email = email;
    }
}
