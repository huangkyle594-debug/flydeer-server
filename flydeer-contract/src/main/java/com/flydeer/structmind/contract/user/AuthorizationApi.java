package com.flydeer.structmind.contract.user;


import com.flydeer.structmind.common.exception.auth.*;
import com.flydeer.structmind.common.exception.business.UserInvalidException;
import com.flydeer.structmind.common.exception.business.UserNotFoundException;
import com.flydeer.structmind.common.exception.frequency.LoginFrequencyException;
import com.flydeer.structmind.common.exception.frequency.SmsFrequencyException;
import com.flydeer.structmind.contract.user.request.*;
import com.flydeer.structmind.contract.user.vo.JwtTokenVO;
import com.flydeer.structmind.contract.user.vo.OauthUrlVO;

public interface AuthorizationApi {

    void sendSmsCode(SendSmsCodeRequest request) throws SmsFrequencyException, SmsSendException;

    JwtTokenVO loginBySms(SmsLoginRequest request)
        throws LoginFrequencyException, SmsVerifyException, UserInvalidException;

    OauthUrlVO oauthLoginUrl(OauthLoginRequest request) throws OauthUrlBuildException;

    JwtTokenVO oauthCallback(OauthCallbackRequest request)
        throws OauthValidateException, OauthExchangeException, UserInvalidException;

    JwtTokenVO refresh(RefreshTokenRequest request)
        throws RefreshTokenParseException, UserNotFoundException, UserInvalidException;
}
