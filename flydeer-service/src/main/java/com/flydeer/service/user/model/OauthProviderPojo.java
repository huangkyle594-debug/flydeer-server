package com.flydeer.service.user.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OauthProviderPojo {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authorizeUrl;
    private String tokenUrl;
    private String userUrl;
    private String scope;
}
