package com.flydeer.structmind.contract.user;


import com.flydeer.structmind.common.exception.auth.*;
import com.flydeer.structmind.common.exception.ratelimit.LoginRateLimitException;
import com.flydeer.structmind.common.exception.ratelimit.SmsRateLimitException;
import com.flydeer.structmind.contract.user.enums.LoginChannel;
import com.flydeer.structmind.contract.user.vo.JwtTokenVO;
import com.flydeer.structmind.contract.user.vo.OauthUrlVO;

public interface AuthorizationApi {

    void sendSmsCode(String phone, String ip) throws SmsRateLimitException, SmsSendException;

    JwtTokenVO loginBySms(String phone, String code, String ip) throws LoginRateLimitException, SmsVerifyException;

    OauthUrlVO authorizeUrl(LoginChannel channel) throws OauthUrlBuildException;

    JwtTokenVO oauthCallback(LoginChannel channel, String code, String state) throws OauthValidateException, OauthExchangeException;

    JwtTokenVO refresh(String refreshToken) throws RefreshTokenParseException;
}
