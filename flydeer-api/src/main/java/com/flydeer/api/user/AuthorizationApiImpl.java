package com.flydeer.api.user;

import com.flydeer.api.user.mapping.AuthorizationMapping;
import com.flydeer.common.exception.auth.*;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.exception.frequency.LoginFrequencyException;
import com.flydeer.common.exception.frequency.SmsFrequencyException;
import com.flydeer.contract.user.AuthorizationApi;
import com.flydeer.contract.user.enums.UserVerifiedStatusEnum;
import com.flydeer.contract.user.request.*;
import com.flydeer.contract.user.vo.JwtTokenVO;
import com.flydeer.contract.user.vo.OauthUrlVO;
import com.flydeer.repository.mysql.dto.UserInfoDTO;
import com.flydeer.service.user.OauthService;
import com.flydeer.service.user.SmsVerifyService;
import com.flydeer.service.user.UserService;
import com.flydeer.service.user.model.OauthUserRecord;
import com.flydeer.service.user.utils.JwtTokenUtils;
import com.flydeer.service.user.utils.RateLimitUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthorizationApiImpl implements AuthorizationApi {

    private final SmsVerifyService smsVerifyService;
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final OauthService oauthService;
    private final RateLimitUtils rateLimiter;

    @Override
    public void sendSmsCode(@Valid SendSmsCodeRequest request) throws SmsFrequencyException, SmsSendException {
        rateLimiter.checkSms(request.getPhone(), request.getIp());
        smsVerifyService.sendVerifyCode(request.getPhone());
    }

    @Override
    public JwtTokenVO loginBySms(@Valid SmsLoginRequest request)
        throws LoginFrequencyException, SmsVerifyException, UserInvalidException {
        rateLimiter.checkLogin("sms:" + request.getPhone() + ":" + request.getIp());
        smsVerifyService.checkVerifyCode(request.getPhone(), request.getCode());
        UserInfoDTO user = userService.loginOrRegisterPhone(request.getPhone());
        return AuthorizationMapping.INSTANCE.jwtToken(jwtTokenUtils.issue(user.getId(), isVerified(user)));
    }

    @Override
    public OauthUrlVO oauthLoginUrl(@Valid OauthLoginRequest request) throws OauthUrlBuildException {
        return AuthorizationMapping.INSTANCE.oauthUrl(oauthService.buildAuthorizeUrl(request.getChannel()));
    }

    @Override
    public JwtTokenVO oauthCallback(@Valid OauthCallbackRequest request)
        throws OauthValidateException, OauthExchangeException, UserInvalidException {
        oauthService.validateState(request.getState());
        OauthUserRecord info = oauthService.exchange(request.getChannel(), request.getCode());
        UserInfoDTO user = userService.loginOrRegisterOauth(request.getChannel(), info);
        return AuthorizationMapping.INSTANCE.jwtToken(jwtTokenUtils.issue(user.getId(), isVerified(user)));
    }

    @Override
    public JwtTokenVO refresh(@Valid RefreshTokenRequest request)
        throws RefreshTokenParseException, UserNotFoundException, UserInvalidException {
        long userId = jwtTokenUtils.parseRefreshToken(request.getRefreshToken());
        UserInfoDTO user = userService.requireActive(userId);
        return AuthorizationMapping.INSTANCE.jwtToken(jwtTokenUtils.issue(user.getId(), isVerified(user)));
    }

    private static boolean isVerified(UserInfoDTO user) {
        return user.getVerified() != null
            && user.getVerified() == UserVerifiedStatusEnum.VERIFIED.getCode();
    }
}
