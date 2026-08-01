package com.flydeer.common.exception.auth;

import com.flydeer.common.exception.ErrorCodes;

public class OauthUrlBuildException extends AuthorizedException {

    public OauthUrlBuildException() {
        super(ErrorCodes.OAUTH_URL_BUILD, "构建授权服务器请求失败");
    }
}
