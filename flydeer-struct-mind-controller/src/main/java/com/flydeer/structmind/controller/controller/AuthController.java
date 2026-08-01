package com.flydeer.structmind.controller.controller;

import com.flydeer.structmind.api.user.AuthorizationApiImpl;
import com.flydeer.structmind.common.exception.ErrorCodes;
import com.flydeer.structmind.common.exception.auth.OauthUrlBuildException;
import com.flydeer.structmind.common.exception.auth.SmsSendException;
import com.flydeer.structmind.common.exception.auth.SmsVerifyException;
import com.flydeer.structmind.common.exception.business.BusinessException;
import com.flydeer.structmind.common.exception.business.UserInvalidException;
import com.flydeer.structmind.common.exception.frequency.LoginFrequencyException;
import com.flydeer.structmind.common.exception.frequency.SmsFrequencyException;
import com.flydeer.structmind.contract.base.response.ApiResult;
import com.flydeer.structmind.contract.user.enums.LoginChannelEnum;
import com.flydeer.structmind.contract.user.request.OauthCallbackRequest;
import com.flydeer.structmind.contract.user.request.OauthLoginRequest;
import com.flydeer.structmind.contract.user.request.SendSmsCodeRequest;
import com.flydeer.structmind.contract.user.request.SmsLoginRequest;
import com.flydeer.structmind.contract.user.vo.JwtTokenVO;
import com.flydeer.structmind.contract.user.vo.OauthUrlVO;
import com.flydeer.structmind.contract.user.vo.TokenResponse;
import com.flydeer.structmind.controller.support.AuthCookieUtils;
import com.flydeer.structmind.service.user.utils.JwtTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthorizationApiImpl authServiceImpl;
    private final AuthCookieUtils authCookieUtils;
    private final AppAuthProperties properties;

    @PostMapping("/sms/send")
    public ApiResult<Void> sendSms(@RequestBody SendSmsCodeRequest request, HttpServletRequest http)
        throws SmsFrequencyException, SmsSendException {
        authServiceImpl.sendSmsCode(request);
        return ApiResult.ok();
    }

    @PostMapping("/sms/login")
    public ApiResult<Void> smsLogin(@Valid @RequestBody SmsLoginRequest request,
                                    HttpServletRequest http, HttpServletResponse response)
        throws SmsVerifyException, UserInvalidException, LoginFrequencyException {
        JwtTokenVO tokens = authServiceImpl.loginBySms(request);
        authCookieUtils.writeRefreshCookie(response, tokens);
        return ApiResult.ok();
    }

    @GetMapping("/{provider}/authorize")
    public ApiResult<OauthUrlVO> authorize(@PathVariable("provider") LoginChannelEnum provider)
        throws OauthUrlBuildException {
        OauthLoginRequest request = new OauthLoginRequest();
        request.setChannel(provider);
        return ApiResult.ok(authServiceImpl.oauthLoginUrl(request));
    }

    @GetMapping("/{provider}/callback")
    public void callback(@PathVariable("provider") String provider, @RequestParam("code") String code,
                         @RequestParam("state") String state, HttpServletResponse response)
        throws IOException {
        OauthCallbackRequest request = new OauthCallbackRequest();


        JwtTokenVO tokens = authServiceImpl.oauthCallback(parseProvider(provider), code, state);
        authCookieUtils.writeRefreshCookie(response, tokens);
        String redirect = properties.getAuth().getFrontendRedirectUrl()
            + (properties.getAuth().getFrontendRedirectUrl().contains("?") ? "&" : "?")
            + "accessToken="
            + URLEncoder.encode(tokens.accessToken(), StandardCharsets.UTF_8);
        response.sendRedirect(redirect);
    }

    @PostMapping("/refresh")
    public ApiResult<TokenResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refresh = authCookieUtils.readRefreshCookie(request);
        if (!StringUtils.hasText(refresh)) {
            throw new BusinessException(ErrorCodes.UNAUTHORIZED, "refresh token missing");
        }
        JwtTokenUtils.IssuedTokens tokens = authServiceImpl.refresh(refresh);
        authCookieUtils.writeRefreshCookie(response, tokens);
        return ApiResult.ok(authServiceImpl.toTokenResponse(tokens));
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout(HttpServletResponse response) {
        authCookieUtils.clearRefreshCookie(response);
        return ApiResult.ok();
    }

    private LoginChannelEnum parseProvider(String provider) {
        try {
            LoginChannelEnum channel = LoginChannelEnum.valueOf(provider.toUpperCase(Locale.ROOT));
            if (channel == LoginChannelEnum.PHONE) {
                throw new BusinessException(ErrorCodes.BAD_REQUEST, "unsupported provider");
            }
            return channel;
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "unsupported provider");
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
