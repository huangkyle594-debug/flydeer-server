package com.flydeer.contract.user;

import com.flydeer.common.exception.auth.NeedVerifyException;
import com.flydeer.common.exception.auth.SmsVerifyException;
import com.flydeer.common.exception.business.BindPhoneExceedException;
import com.flydeer.common.exception.business.PhoneChannelOperateException;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.contract.common.request.ApiRequest;
import com.flydeer.contract.user.request.BindPhoneRequest;
import com.flydeer.contract.user.request.UpdateUserNameRequest;
import com.flydeer.contract.user.vo.JwtTokenVO;
import com.flydeer.contract.user.vo.UserProfileVO;

public interface UserMangeApi {

    UserProfileVO me(ApiRequest request) throws UserNotFoundException, UserInvalidException, NeedVerifyException;

    /**
     * Update display name and re-issue tokens with the new name claim.
     */
    JwtTokenVO updateName(UpdateUserNameRequest request)
        throws UserNotFoundException, UserInvalidException, NeedVerifyException;

    JwtTokenVO bindPhone(BindPhoneRequest request) throws UserNotFoundException, SmsVerifyException,
        UserInvalidException, BindPhoneExceedException, PhoneChannelOperateException, NeedVerifyException;

    /**
     * Self-cancel: delete account + {@code UserDeletedEvent}.
     */
    void cancel(ApiRequest request) throws UserNotFoundException;
}
