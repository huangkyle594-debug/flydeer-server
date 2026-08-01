package com.flydeer.api.user;

import com.flydeer.api.user.mapper.AuthorizationMapper;
import com.flydeer.common.exception.auth.SmsVerifyException;
import com.flydeer.common.exception.business.BindPhoneExceedException;
import com.flydeer.common.exception.business.PhoneChannelOperateException;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.utils.PhoneNumberUtils;
import com.flydeer.contract.base.request.ApiRequest;
import com.flydeer.contract.user.UserMangeApi;
import com.flydeer.contract.user.request.BindPhoneRequest;
import com.flydeer.contract.user.request.UpdateUserRequest;
import com.flydeer.contract.user.vo.JwtTokenVO;
import com.flydeer.contract.user.vo.UserProfileVO;
import com.flydeer.repository.mysql.dto.UserInfoDTO;
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
        throws UserNotFoundException, UserInvalidException {
        UserInfoDTO user = userService.requireActive(request.getUserId());
        return new UserProfileVO(
            user.getId(),
            user.getChannel(),
            user.getName(),
            user.getVerified() != null && user.getVerified() == 1,
            PhoneNumberUtils.maskPhone(user.getPhone()));
    }

    @Override
    public UserProfileVO update(@Valid UpdateUserRequest request)
        throws UserNotFoundException, UserInvalidException {
        userService.updateUserName(request.getUserId(), request.getName());
        return me(request);
    }

    @Override
    public JwtTokenVO bindPhone(@Valid BindPhoneRequest request)
        throws UserNotFoundException, SmsVerifyException, UserInvalidException,
        BindPhoneExceedException, PhoneChannelOperateException {
        smsVerifyService.checkVerifyCode(request.getPhone(), request.getCode());
        userService.bindPhone(request.getUserId(), request.getPhone());
        return AuthorizationMapper.INSTANCE.jwtToken(jwtTokenUtils.issue(request.getUserId(), true));
    }
}
