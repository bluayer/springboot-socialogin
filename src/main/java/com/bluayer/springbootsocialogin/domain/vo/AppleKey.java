package com.bluayer.springbootsocialogin.domain.vo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppleKey {

    private String kty;
    private String kid;
    private String use;
    private String alg;
    private String n;
    private String e;

}