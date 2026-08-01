package com.flydeer.structmind.api.user;

import com.flydeer.structmind.api.user.mapper.AuthorizationMapper;
import com.flydeer.structmind.common.exception.auth.*;
import com.flydeer.structmind.common.exception.business.UserInvalidException;
import com.flydeer.structmind.common.exception.business.UserNotFoundException;
import com.flydeer.structmind.common.exception.frequency.LoginFrequencyException;
import com.flydeer.structmind.common.exception.frequency.SmsFrequencyException;
import com.flydeer.structmind.contract.user.AuthorizationApi;
import com.flydeer.structmind.contract.user.enums.UserVerifiedStatusEnum;
import com.flydeer.structmind.contract.user.request.*;
import com.flydeer.structmind.contract.user.vo.JwtTokenVO;
import com.flydeer.structmind.contract.user.vo.OauthUrlVO;
import com.flydeer.structmind.repository.mysql.dto.UserInfoDTO;
import com.flydeer.structmind.service.user.OauthService;
import com.flydeer.structmind.service.user.SmsVerifyService;
import com.flydeer.structmind.service.user.UserService;
import com.flydeer.structmind.service.user.model.OauthUserRecord;
import com.flydeer.structmind.service.user.utils.JwtTokenUtils;
import com.flydeer.structmind.service.user.utils.RateLimitUtils;
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
        return AuthorizationMapper.INSTANCE.jwtToken(jwtTokenUtils.issue(user.getId(), isVerified(user)));
    }

    @Override
    public OauthUrlVO oauthLoginUrl(@Valid OauthLoginRequest request) throws OauthUrlBuildException {
        return AuthorizationMapper.INSTANCE.oauthUrl(oauthService.buildAuthorizeUrl(request.getChannel()));
    }

    @Override
    public JwtTokenVO oauthCallback(@Valid OauthCallbackRequest request)
        throws OauthValidateException, OauthExchangeException, UserInvalidException {
        oauthService.validateState(request.getState());
        OauthUserRecord info = oauthService.exchange(request.getChannel(), request.getCode());
        UserInfoDTO user = userService.loginOrRegisterOauth(request.getChannel(), info);
        return AuthorizationMapper.INSTANCE.jwtToken(jwtTokenUtils.issue(user.getId(), isVerified(user)));
    }

    @Override
    public JwtTokenVO refresh(@Valid RefreshTokenRequest request)
        throws RefreshTokenParseException, UserNotFoundException, UserInvalidException {
        long userId = jwtTokenUtils.parseRefreshToken(request.getRefreshToken());
        UserInfoDTO user = userService.requireActive(userId);
        return AuthorizationMapper.INSTANCE.jwtToken(jwtTokenUtils.issue(user.getId(), isVerified(user)));
    }

    private static boolean isVerified(UserInfoDTO user) {
        return user.getVerified() != null
            && user.getVerified() == UserVerifiedStatusEnum.VERIFIED.getCode();
    }
}
