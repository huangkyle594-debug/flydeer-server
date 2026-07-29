package com.flydeer.structmind.api.auth;

import com.flydeer.structmind.contract.auth.AuthorizeUrlResponse;
import com.flydeer.structmind.contract.auth.TokenResponse;
import com.flydeer.structmind.contract.enums.LoginChannel;
import com.flydeer.structmind.repository.entity.UserEntity;
import com.flydeer.structmind.service.auth.JwtTokenService;
import com.flydeer.structmind.service.oauth.OauthClientService;
import com.flydeer.structmind.service.oauth.OauthUserInfo;
import com.flydeer.structmind.service.ratelimit.LocalRateLimiter;
import com.flydeer.structmind.service.sms.SmsVerifyClient;
import com.flydeer.structmind.service.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class AuthFacade {

    private final SmsVerifyClient smsVerifyClient;
    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final OauthClientService oauthClientService;
    private final LocalRateLimiter rateLimiter;

    public AuthFacade(
            SmsVerifyClient smsVerifyClient,
            UserService userService,
            JwtTokenService jwtTokenService,
            OauthClientService oauthClientService,
            LocalRateLimiter rateLimiter) {
        this.smsVerifyClient = smsVerifyClient;
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.oauthClientService = oauthClientService;
        this.rateLimiter = rateLimiter;
    }

    public void sendSmsCode(String phone, String ip) {
        rateLimiter.checkSms(phone, ip);
        smsVerifyClient.sendVerifyCode(phone);
    }

    public JwtTokenService.IssuedTokens loginBySms(String phone, String code, String ip) {
        rateLimiter.checkLogin("sms:" + phone + ":" + ip);
        smsVerifyClient.checkVerifyCode(phone, code);
        UserEntity user = userService.loginOrRegisterPhone(phone);
        return jwtTokenService.issue(user.getId());
    }

    public AuthorizeUrlResponse authorizeUrl(LoginChannel channel) {
        return new AuthorizeUrlResponse(oauthClientService.buildAuthorizeUrl(channel));
    }

    public JwtTokenService.IssuedTokens oauthCallback(LoginChannel channel, String code, String state) {
        oauthClientService.validateState(state);
        OauthUserInfo info = oauthClientService.exchange(channel, code);
        UserEntity user = userService.loginOrRegisterOauth(channel, info);
        return jwtTokenService.issue(user.getId());
    }

    public JwtTokenService.IssuedTokens refresh(String refreshToken) {
        long userId = jwtTokenService.parseUserId(refreshToken, JwtTokenService.TYP_REFRESH);
        userService.requireActive(userId);
        return jwtTokenService.issue(userId);
    }

    public TokenResponse toTokenResponse(JwtTokenService.IssuedTokens tokens) {
        return new TokenResponse(tokens.accessToken(), tokens.expiresInSeconds());
    }
}
