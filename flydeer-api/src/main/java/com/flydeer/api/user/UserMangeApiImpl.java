package com.flydeer.api.user;

import com.flydeer.api.user.mapping.AuthorizationMapping;
import com.flydeer.common.exception.auth.NeedVerifyException;
import com.flydeer.common.exception.auth.SmsVerifyException;
import com.flydeer.common.exception.business.BindPhoneExceedException;
import com.flydeer.common.exception.business.PhoneChannelOperateException;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.contract.common.request.ApiRequest;
import com.flydeer.contract.user.UserMangeApi;
import com.flydeer.contract.user.enums.UserVerifiedStatusEnum;
import com.flydeer.contract.user.request.BindPhoneRequest;
import com.flydeer.contract.user.request.UpdateUserNameRequest;
import com.flydeer.contract.user.vo.JwtTokenVO;
import com.flydeer.contract.user.vo.UserProfileVO;
import com.flydeer.repository.mysql.dto.UserInfoDTO;
import com.flydeer.repository.mysql.option.user.UserOptions;
import com.flydeer.service.user.SmsVerifyService;
import com.flydeer.service.user.UserService;
import com.flydeer.service.user.utils.JwtTokenUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserMangeApiImpl implements UserMangeApi {

    private final UserService userService;
    private final SmsVerifyService smsVerifyService;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    public UserProfileVO me(@Valid ApiRequest request)
        throws UserNotFoundException, UserInvalidException, NeedVerifyException {
        UserInfoDTO user = userService.queryUser(request.getUserId(), UserOptions.option().requireActive());
        return new UserProfileVO(
            user.getId(),
            user.getChannel(),
            user.getName(),
            user.getVerified() != null && user.getVerified() == 1,
            user.getPhone());
    }

    @Override
    public JwtTokenVO updateName(@Valid UpdateUserNameRequest request)
        throws UserNotFoundException, UserInvalidException, NeedVerifyException {
        UserInfoDTO user = userService.updateUserName(request.getUserId(), request.getName());
        return AuthorizationMapping.INSTANCE.jwtToken(
            jwtTokenUtils.issue(user.getId(), isVerified(user), user.getStatus(), user.getName()));
    }

    @Override
    public JwtTokenVO bindPhone(@Valid BindPhoneRequest request)
        throws UserNotFoundException, SmsVerifyException,
        BindPhoneExceedException, PhoneChannelOperateException, UserInvalidException, NeedVerifyException {
        smsVerifyService.checkVerifyCode(request.getPhone(), request.getCode());
        UserInfoDTO user = userService.bindPhone(request.getUserId(), request.getPhone());
        return AuthorizationMapping.INSTANCE.jwtToken(
            jwtTokenUtils.issue(user.getId(), true, user.getStatus(), user.getName()));
    }

    @Override
    public void cancel(@Valid ApiRequest request) throws UserNotFoundException {
        userService.deleteUser(request.getUserId());
    }

    private static boolean isVerified(UserInfoDTO user) {
        return user.getVerified() != null
            && user.getVerified().equals(UserVerifiedStatusEnum.VERIFIED.getCode());
    }
}
