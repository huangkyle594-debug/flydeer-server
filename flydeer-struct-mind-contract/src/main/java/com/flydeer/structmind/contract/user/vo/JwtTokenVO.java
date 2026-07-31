package com.flydeer.structmind.contract.user.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtTokenVO {

    private String accessToken;
    private String refreshToken;
    private Long expiresInSeconds;
}
