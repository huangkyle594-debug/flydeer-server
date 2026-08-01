package com.flydeer.structmind.api.user;

import com.flydeer.structmind.common.exception.auth.SmsVerifyException;
import com.flydeer.structmind.common.exception.business.BindPhoneExceedException;
import com.flydeer.structmind.common.exception.business.PhoneChannelOperateException;
import com.flydeer.structmind.common.exception.business.UserInvalidException;
import com.flydeer.structmind.common.exception.business.UserNotFoundException;
import com.flydeer.structmind.common.utils.PhoneNumberUtils;
import com.flydeer.structmind.contract.base.request.ApiRequest;
import com.flydeer.structmind.contract.user.UserMangeApi;
import com.flydeer.structmind.contract.user.request.BindPhoneRequest;
import com.flydeer.structmind.contract.user.request.UpdateUserRequest;
import com.flydeer.structmind.contract.user.vo.UserProfileVO;
import com.flydeer.structmind.repository.mysql.dto.UserInfoDTO;
import com.flydeer.structmind.service.user.SmsVerifyService;
import com.flydeer.structmind.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserMangeApiImpl implements UserMangeApi {

    private final UserService userService;
    private final SmsVerifyService smsVerifyService;

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
    public void bindPhone(@Valid BindPhoneRequest request)
        throws UserNotFoundException, SmsVerifyException, UserInvalidException,
        BindPhoneExceedException, PhoneChannelOperateException {
        smsVerifyService.checkVerifyCode(request.getPhone(), request.getCode());
        userService.bindPhone(request.getUserId(), request.getPhone());
    }
}
