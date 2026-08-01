package com.flydeer.structmind.contract.user;

import com.flydeer.structmind.common.exception.auth.SmsVerifyException;
import com.flydeer.structmind.common.exception.business.BindPhoneExceedException;
import com.flydeer.structmind.common.exception.business.PhoneChannelOperateException;
import com.flydeer.structmind.common.exception.business.UserInvalidException;
import com.flydeer.structmind.common.exception.business.UserNotFoundException;
import com.flydeer.structmind.contract.base.request.ApiRequest;
import com.flydeer.structmind.contract.user.request.BindPhoneRequest;
import com.flydeer.structmind.contract.user.request.UpdateUserRequest;
import com.flydeer.structmind.contract.user.vo.JwtTokenVO;
import com.flydeer.structmind.contract.user.vo.UserProfileVO;

public interface UserMangeApi {

    UserProfileVO me(ApiRequest request) throws UserNotFoundException, UserInvalidException;

    UserProfileVO update(UpdateUserRequest request) throws UserNotFoundException, UserInvalidException;

    JwtTokenVO bindPhone(BindPhoneRequest request) throws UserNotFoundException, SmsVerifyException,
        UserInvalidException, BindPhoneExceedException, PhoneChannelOperateException;
}
