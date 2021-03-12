package com.bluayer.springbootsocialogin.domain.verifier;

import com.bluayer.springbootsocialogin.dto.UserVerifiedResponse;
import com.bluayer.springbootsocialogin.dto.VerifyRequest;

public interface UserAccessTokenVerifier {
    UserVerifiedResponse verify(VerifyRequest request);
}
