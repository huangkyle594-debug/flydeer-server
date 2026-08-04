package com.flydeer.contract.user;


import com.flydeer.common.exception.auth.*;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.exception.frequency.LoginFrequencyException;
import com.flydeer.common.exception.frequency.SmsFrequencyException;
import com.flydeer.contract.user.request.*;
import com.flydeer.contract.user.vo.JwtTokenVO;
import com.flydeer.contract.user.vo.OauthUrlVO;

public interface AuthorizationApi {

    void sendSmsCode(SendSmsCodeRequest request) throws SmsFrequencyException, SmsSendException;

    JwtTokenVO loginBySms(SmsLoginRequest request)
        throws LoginFrequencyException, SmsVerifyException, UserInvalidException;

    OauthUrlVO oauthLoginUrl(OauthLoginRequest request) throws OauthUrlBuildException;

    JwtTokenVO oauthCallback(OauthCallbackRequest request)
        throws OauthValidateException, OauthExchangeException, UserInvalidException;

    JwtTokenVO refresh(RefreshTokenRequest request)
        throws RefreshTokenParseException, UserNotFoundException, UserInvalidException, NeedVerifyException;
}
