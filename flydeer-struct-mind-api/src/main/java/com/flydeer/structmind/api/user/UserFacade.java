package com.flydeer.structmind.api.user;

import com.flydeer.structmind.contract.auth.TokenResponse;
import com.flydeer.structmind.contract.user.UserProfileResponse;
import com.flydeer.structmind.repository.entity.UserEntity;
import com.flydeer.structmind.service.auth.JwtTokenService;
import com.flydeer.structmind.service.sms.SmsVerifyClient;
import com.flydeer.structmind.service.user.UserService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserFacade {

    private final UserService userService;
    private final SmsVerifyClient smsVerifyClient;
    private final JwtTokenService jwtTokenService;

    public UserFacade(
            UserService userService, SmsVerifyClient smsVerifyClient, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.smsVerifyClient = smsVerifyClient;
        this.jwtTokenService = jwtTokenService;
    }

    public UserProfileResponse me(Long userId) {
        UserEntity user = userService.requireActive(userId);
        List<Long> delegated = userService.listDelegatedUserIds(userId);
        return new UserProfileResponse(
                user.getId(),
                user.getChannel(),
                user.getNickname(),
                user.getVerified() != null && user.getVerified() == 1,
                user.getPhone(),
                delegated);
    }

    public UserProfileResponse updateNickname(Long userId, String nickName) {
        userService.updateNickname(userId, nickName);
        return me(userId);
    }

    public JwtTokenService.IssuedTokens bindPhone(Long userId, String phone, String code) {
        smsVerifyClient.checkVerifyCode(phone, code);
        userService.bindPhone(userId, phone);
        return jwtTokenService.issue(userId);
    }

    public TokenResponse toTokenResponse(JwtTokenService.IssuedTokens tokens) {
        return new TokenResponse(tokens.accessToken(), tokens.expiresInSeconds());
    }
}
