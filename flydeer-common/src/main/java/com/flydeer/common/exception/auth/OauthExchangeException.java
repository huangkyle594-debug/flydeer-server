package com.flydeer.common.exception.auth;

import com.flydeer.common.exception.ErrorCodes;

public class OauthExchangeException extends AuthorizedException {

    public OauthExchangeException() {
        super(ErrorCodes.OAUTH_EXCHANGE, "第三方验证失败");
    }
}
