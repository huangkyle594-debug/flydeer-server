package com.flydeer.contract.user;

import com.flydeer.common.exception.auth.NeedVerifyException;
import com.flydeer.common.exception.auth.SmsVerifyException;
import com.flydeer.common.exception.business.BindPhoneExceedException;
import com.flydeer.common.exception.business.PhoneChannelOperateException;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.contract.common.request.ApiRequest;
import com.flydeer.contract.user.request.BindPhoneRequest;
import com.flydeer.contract.user.request.DisableUserRequest;
import com.flydeer.contract.user.request.UpdateUserRequest;
import com.flydeer.contract.user.vo.JwtTokenVO;
import com.flydeer.contract.user.vo.UserProfileVO;

public interface UserMangeApi {

    UserProfileVO me(ApiRequest request) throws UserNotFoundException, UserInvalidException, NeedVerifyException;

    UserProfileVO update(UpdateUserRequest request) throws UserNotFoundException, UserInvalidException, NeedVerifyException;

    JwtTokenVO bindPhone(BindPhoneRequest request) throws UserNotFoundException, SmsVerifyException,
        UserInvalidException, BindPhoneExceedException, PhoneChannelOperateException, NeedVerifyException;

    void disable(DisableUserRequest request) throws UserNotFoundException;

    /** Self-cancel: delete account + {@code UserDeletedEvent}. */
    void cancel(ApiRequest request) throws UserNotFoundException;
}
