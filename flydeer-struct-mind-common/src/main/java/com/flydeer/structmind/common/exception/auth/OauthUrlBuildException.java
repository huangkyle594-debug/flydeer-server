package com.flydeer.structmind.common.exception.auth;

import com.flydeer.structmind.common.error.ErrorCodes;

public class OauthUrlBuildException extends AuthorizedException {

    public OauthUrlBuildException() {
        super(ErrorCodes.OAUTH_URL_BUILD, "构建授权服务器请求失败");
    }
}
