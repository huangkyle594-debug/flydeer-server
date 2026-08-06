package com.flydeer.controller.controller;

import com.flydeer.common.enums.AuthRequiredLevel;
import com.flydeer.common.enums.AuthResolveLevel;
import com.flydeer.common.exception.auth.NeedVerifyException;
import com.flydeer.common.exception.auth.SmsSendException;
import com.flydeer.common.exception.auth.SmsVerifyException;
import com.flydeer.common.exception.business.BindPhoneExceedException;
import com.flydeer.common.exception.business.PhoneChannelOperateException;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.exception.frequency.SmsFrequencyException;
import com.flydeer.common.utils.IpUtils;
import com.flydeer.contract.common.request.ApiRequest;
import com.flydeer.contract.common.response.ApiResult;
import com.flydeer.contract.user.AuthorizationApi;
import com.flydeer.contract.user.UserMangeApi;
import com.flydeer.contract.user.request.BindPhoneRequest;
import com.flydeer.contract.user.request.SendSmsCodeRequest;
import com.flydeer.contract.user.request.UpdateUserNameRequest;
import com.flydeer.contract.user.vo.JwtTokenVO;
import com.flydeer.contract.user.vo.UserProfileVO;
import com.flydeer.controller.aop.AuthCheck;
import com.flydeer.controller.utils.AuthCookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserMangeApi userMangeApi;
    private final AuthorizationApi authorizationApi;
    private final AuthCookieUtils authCookieUtils;

    @GetMapping("/me")
    public ApiResult<UserProfileVO> me(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.AUTHENTICATED) ApiRequest apiRequest)
        throws UserNotFoundException, UserInvalidException, NeedVerifyException {

        return ApiResult.ok(userMangeApi.me(apiRequest));
    }

    @PostMapping("/me/name")
    public ApiResult<JwtTokenVO> updateName(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody UpdateUserNameRequest body,
        HttpServletResponse response)
        throws UserNotFoundException, UserInvalidException, NeedVerifyException {

        UpdateUserNameRequest request = new UpdateUserNameRequest(apiRequest);
        request.setName(body.getName());
        JwtTokenVO token = userMangeApi.updateName(request);
        authCookieUtils.writeRefreshCookie(response, token);
        return ApiResult.ok(token.clearRefreshToken());
    }

    @PostMapping("/me/phone/send")
    public ApiResult<Void> sendSms(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.AUTHENTICATED) ApiRequest apiRequest,
        @RequestBody SendSmsCodeRequest body,
        HttpServletRequest http)
        throws SmsFrequencyException, SmsSendException {

        SendSmsCodeRequest request = new SendSmsCodeRequest(apiRequest);
        request.setPhone(body.getPhone());
        request.setIp(IpUtils.clientIp(http));
        authorizationApi.sendSmsCode(request);
        return ApiResult.ok();
    }

    @PostMapping("/me/phone/bind")
    public ApiResult<JwtTokenVO> bindPhone(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.AUTHENTICATED) ApiRequest apiRequest,
        @RequestBody BindPhoneRequest body,
        HttpServletResponse response)
        throws UserNotFoundException, SmsVerifyException, UserInvalidException,
        BindPhoneExceedException, PhoneChannelOperateException, NeedVerifyException {

        BindPhoneRequest request = new BindPhoneRequest(apiRequest);
        request.setPhone(body.getPhone());
        request.setCode(body.getCode());
        JwtTokenVO token = userMangeApi.bindPhone(request);
        authCookieUtils.writeRefreshCookie(response, token);
        return ApiResult.ok(token.clearRefreshToken());
    }

    @PostMapping("/me/cancel")
    public ApiResult<Void> cancel(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.AUTHENTICATED) ApiRequest apiRequest,
        HttpServletResponse response)
        throws UserNotFoundException {

        userMangeApi.cancel(apiRequest);
        authCookieUtils.clearRefreshCookie(response);
        return ApiResult.ok();
    }
}
