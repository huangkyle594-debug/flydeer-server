package com.flydeer.structmind.api.auth;

import com.flydeer.structmind.contract.auth.AuthorizeUrlResponse;
import com.flydeer.structmind.contract.auth.TokenResponse;
import com.flydeer.structmind.contract.enums.LoginChannel;
import com.flydeer.structmind.repository.entity.UserEntity;
import com.flydeer.structmind.service.model.user.IssuedTokensRecord;
import com.flydeer.structmind.service.model.user.OauthUserRecord;
import com.flydeer.structmind.service.service.user.OauthService;
import com.flydeer.structmind.service.service.user.SmsVerifyService;
import com.flydeer.structmind.service.service.user.UserService;
import com.flydeer.structmind.service.utils.JwtTokenUtils;
import com.flydeer.structmind.service.utils.RateLimitUtils;
import org.springframework.stereotype.Service;

@Service
public class AuthFacade {

    private final SmsVerifyService smsVerifyService;
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final OauthService oauthService;
    private final RateLimitUtils rateLimiter;

    public AuthFacade(
        SmsVerifyService smsVerifyService,
        UserService userService,
        JwtTokenUtils jwtTokenUtils,
        OauthService oauthService,
        RateLimitUtils rateLimiter) {
        this.smsVerifyService = smsVerifyService;
        this.userService = userService;
        this.jwtTokenUtils = jwtTokenUtils;
        this.oauthService = oauthService;
        this.rateLimiter = rateLimiter;
    }

    public void sendSmsCode(String phone, String ip) {
        rateLimiter.checkSms(phone, ip);
        smsVerifyService.sendVerifyCode(phone);
    }

    public IssuedTokensRecord loginBySms(String phone, String code, String ip) {
        rateLimiter.checkLogin("sms:" + phone + ":" + ip);
        smsVerifyService.checkVerifyCode(phone, code);
        UserEntity user = userService.loginOrRegisterPhone(phone);
        return jwtTokenUtils.issue(user.getId());
    }

    public AuthorizeUrlResponse authorizeUrl(LoginChannel channel) {
        return new AuthorizeUrlResponse(oauthService.buildAuthorizeUrl(channel));
    }

    public IssuedTokensRecord oauthCallback(LoginChannel channel, String code, String state) {
        oauthService.validateState(state);
        OauthUserRecord info = oauthService.exchange(channel, code);
        UserEntity user = userService.loginOrRegisterOauth(channel, info);
        return jwtTokenUtils.issue(user.getId());
    }

    public IssuedTokensRecord refresh(String refreshToken) {
        long userId = jwtTokenUtils.parseRefreshToken(refreshToken);
        userService.requireActive(userId);
        return jwtTokenUtils.issue(userId);
    }

    public TokenResponse toTokenResponse(IssuedTokensRecord tokens) {
        return new TokenResponse(tokens.accessToken(), tokens.expiresInSeconds());
    }
}
