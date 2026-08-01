package com.flydeer.structmind.contract.user;


import com.flydeer.structmind.common.exception.auth.*;
import com.flydeer.structmind.common.exception.business.UserInvalidException;
import com.flydeer.structmind.common.exception.business.UserNotFoundException;
import com.flydeer.structmind.common.exception.ratelimit.LoginRateLimitException;
import com.flydeer.structmind.common.exception.ratelimit.SmsRateLimitException;
import com.flydeer.structmind.contract.user.request.*;
import com.flydeer.structmind.contract.user.vo.JwtTokenVO;
import com.flydeer.structmind.contract.user.vo.OauthUrlVO;

public interface AuthorizationApi {

    void sendSmsCode(SendSmsCodeRequest request) throws SmsRateLimitException, SmsSendException;

    JwtTokenVO loginBySms(SmsLoginRequest request)
        throws LoginRateLimitException, SmsVerifyException, UserInvalidException;

    OauthUrlVO oauthLoginUrl(OauthLoginRequest request) throws OauthUrlBuildException;

    JwtTokenVO oauthCallback(OauthCallbackRequest request)
        throws OauthValidateException, OauthExchangeException, UserInvalidException;

    JwtTokenVO refresh(RefreshTokenRequest request)
        throws RefreshTokenParseException, UserNotFoundException, UserInvalidException;
}
