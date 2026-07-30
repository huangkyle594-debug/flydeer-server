package com.flydeer.structmind.api.user;

import com.flydeer.structmind.contract.auth.TokenResponse;
import com.flydeer.structmind.contract.user.UserProfileResponse;
import com.flydeer.structmind.repository.entity.UserEntity;
import com.flydeer.structmind.service.utils.JwtTokenUtils;
import com.flydeer.structmind.service.sms.SmsVerifyClient;
import com.flydeer.structmind.service.service.user.UserService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserFacade {

    private final UserService userService;
    private final SmsVerifyClient smsVerifyClient;
    private final JwtTokenUtils jwtTokenUtils;

    public UserFacade(
            UserService userService, SmsVerifyClient smsVerifyClient, JwtTokenUtils jwtTokenUtils) {
        this.userService = userService;
        this.smsVerifyClient = smsVerifyClient;
        this.jwtTokenUtils = jwtTokenUtils;
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

    public JwtTokenUtils.IssuedTokens bindPhone(Long userId, String phone, String code) {
        smsVerifyClient.checkVerifyCode(phone, code);
        userService.bindPhone(userId, phone);
        return jwtTokenUtils.issue(userId);
    }

    public TokenResponse toTokenResponse(JwtTokenUtils.IssuedTokens tokens) {
        return new TokenResponse(tokens.accessToken(), tokens.expiresInSeconds());
    }
}
