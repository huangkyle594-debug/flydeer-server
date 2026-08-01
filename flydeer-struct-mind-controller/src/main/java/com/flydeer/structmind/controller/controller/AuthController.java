package com.flydeer.structmind.controller.controller;

import com.flydeer.structmind.common.exception.auth.*;
import com.flydeer.structmind.common.exception.business.UserInvalidException;
import com.flydeer.structmind.common.exception.business.UserNotFoundException;
import com.flydeer.structmind.common.exception.frequency.LoginFrequencyException;
import com.flydeer.structmind.common.exception.frequency.SmsFrequencyException;
import com.flydeer.structmind.common.utils.IpUtils;
import com.flydeer.structmind.contract.base.request.ApiRequest;
import com.flydeer.structmind.contract.base.response.ApiResult;
import com.flydeer.structmind.contract.user.AuthorizationApi;
import com.flydeer.structmind.contract.user.enums.LoginChannelEnum;
import com.flydeer.structmind.contract.user.request.*;
import com.flydeer.structmind.contract.user.vo.JwtTokenVO;
import com.flydeer.structmind.contract.user.vo.OauthUrlVO;
import com.flydeer.structmind.controller.aop.AuthCheck;
import com.flydeer.structmind.controller.utils.AuthCookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthorizationApi authorizationApi;
    private final AuthCookieUtils authCookieUtils;

    @PostMapping("/sms/send")
    public ApiResult<Void> sendSms(
        @AuthCheck ApiRequest apiRequest,
        @RequestBody SendSmsCodeRequest body,
        HttpServletRequest http)
        throws SmsFrequencyException, SmsSendException {

        SendSmsCodeRequest request = new SendSmsCodeRequest(apiRequest);
        request.setPhone(body.getPhone());
        request.setIp(IpUtils.clientIp(http));
        authorizationApi.sendSmsCode(request);
        return ApiResult.ok();
    }

    @PostMapping("/sms/login")
    public ApiResult<JwtTokenVO> smsLogin(
        @AuthCheck ApiRequest apiRequest,
        @RequestBody SmsLoginRequest body,
        HttpServletRequest http,
        HttpServletResponse response)
        throws SmsVerifyException, UserInvalidException, LoginFrequencyException {

        SmsLoginRequest request = new SmsLoginRequest(apiRequest);
        request.setIp(IpUtils.clientIp(http));
        request.setPhone(body.getPhone());
        request.setCode(body.getCode());
        JwtTokenVO tokens = authorizationApi.loginBySms(request);
        authCookieUtils.writeRefreshCookie(response, tokens);
        return ApiResult.ok(tokens.clearRefreshToken());
    }

    @GetMapping("/{provider}/authorize")
    public ApiResult<OauthUrlVO> authorize(
        @AuthCheck ApiRequest apiRequest,
        @PathVariable("provider") String provider)
        throws OauthUrlBuildException {

        OauthLoginRequest request = new OauthLoginRequest(apiRequest);
        request.setChannel(LoginChannelEnum.valueOf(provider));
        return ApiResult.ok(authorizationApi.oauthLoginUrl(request));
    }

    @GetMapping("/{provider}/callback")
    public void callback(
        @AuthCheck ApiRequest apiRequest,
        @PathVariable("provider") String provider,
        @RequestParam("code") String code,
        @RequestParam("state") String state,
        HttpServletResponse response)
        throws IOException, OauthValidateException, OauthExchangeException, UserInvalidException {

        OauthCallbackRequest request = new OauthCallbackRequest(apiRequest);
        request.setChannel(LoginChannelEnum.valueOf(provider));
        request.setCode(code);
        request.setState(state);
        JwtTokenVO tokens = authorizationApi.oauthCallback(request);
        authCookieUtils.writeRefreshCookie(response, tokens);
        String redirectBase = authCookieUtils.getRedirectUrl();
        String redirect = redirectBase
            + (redirectBase.contains("?") ? "&" : "?")
            + "accessToken=" + URLEncoder.encode(tokens.getAccessToken(), StandardCharsets.UTF_8);
        response.sendRedirect(redirect);
    }

    @PostMapping("/refresh")
    public ApiResult<JwtTokenVO> refresh(
        @AuthCheck ApiRequest apiRequest,
        HttpServletRequest request,
        HttpServletResponse response)
        throws RefreshTokenParseException, UserNotFoundException, UserInvalidException, NeedLoginException {

        String refresh = authCookieUtils.readRefreshCookie(request);
        if (!StringUtils.hasText(refresh)) {
            throw new NeedLoginException();
        }
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(apiRequest);
        refreshRequest.setRefreshToken(refresh);
        JwtTokenVO tokens = authorizationApi.refresh(refreshRequest);
        authCookieUtils.writeRefreshCookie(response, tokens);
        return ApiResult.ok(tokens.clearRefreshToken());
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout(HttpServletResponse response) {
        authCookieUtils.clearRefreshCookie(response);
        return ApiResult.ok();
    }
}
