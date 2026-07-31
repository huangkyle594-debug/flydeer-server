package com.flydeer.structmind.api.user;

import com.flydeer.structmind.api.user.mapper.AuthorizationMapper;
import com.flydeer.structmind.common.exception.auth.*;
import com.flydeer.structmind.common.exception.ratelimit.LoginRateLimitException;
import com.flydeer.structmind.common.exception.ratelimit.SmsRateLimitException;
import com.flydeer.structmind.contract.user.AuthorizationApi;
import com.flydeer.structmind.contract.user.enums.LoginChannel;
import com.flydeer.structmind.contract.user.vo.JwtTokenVO;
import com.flydeer.structmind.contract.user.vo.OauthUrlVO;
import com.flydeer.structmind.contract.user.vo.TokenResponse;
import com.flydeer.structmind.repository.mysql.entity.UserEntity;
import com.flydeer.structmind.service.user.OauthService;
import com.flydeer.structmind.service.user.SmsVerifyService;
import com.flydeer.structmind.service.user.UserService;
import com.flydeer.structmind.service.user.model.IssuedTokensRecord;
import com.flydeer.structmind.service.user.model.OauthUserRecord;
import com.flydeer.structmind.service.user.utils.JwtTokenUtils;
import com.flydeer.structmind.service.user.utils.RateLimitUtils;
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
    public void sendSmsCode(String phone, String ip) throws SmsRateLimitException, SmsSendException {
        rateLimiter.checkSms(phone, ip);
        smsVerifyService.sendVerifyCode(phone);
    }

    @Override
    public JwtTokenVO loginBySms(String phone, String code, String ip)
        throws LoginRateLimitException, SmsVerifyException {
        rateLimiter.checkLogin("sms:" + phone + ":" + ip);
        smsVerifyService.checkVerifyCode(phone, code);
        UserEntity user = userService.loginOrRegisterPhone(phone);
        return AuthorizationMapper.INSTANCE.jwtToken(jwtTokenUtils.issue(user.getId()));
    }

    @Override
    public OauthUrlVO authorizeUrl(LoginChannel channel) throws OauthUrlBuildException {
        return AuthorizationMapper.INSTANCE.oauthUrl(oauthService.buildAuthorizeUrl(channel));
    }

    @Override
    public JwtTokenVO oauthCallback(LoginChannel channel, String code, String state)
        throws OauthValidateException, OauthExchangeException {
        oauthService.validateState(state);
        OauthUserRecord info = oauthService.exchange(channel, code);
        UserEntity user = userService.loginOrRegisterOauth(channel, info);
        return AuthorizationMapper.INSTANCE.jwtToken(jwtTokenUtils.issue(user.getId()));
    }

    @Override
    public JwtTokenVO refresh(String refreshToken) throws RefreshTokenParseException {
        long userId = jwtTokenUtils.parseRefreshToken(refreshToken);
        userService.requireActive(userId);
        return AuthorizationMapper.INSTANCE.jwtToken(jwtTokenUtils.issue(userId));
    }

    public TokenResponse toTokenResponse(IssuedTokensRecord tokens) {
        return new TokenResponse(tokens.accessToken(), tokens.expiresInSeconds());
    }
}
