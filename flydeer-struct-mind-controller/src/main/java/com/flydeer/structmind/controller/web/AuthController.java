package com.flydeer.structmind.controller.web;

import com.flydeer.structmind.api.user.AuthorizationApiImpl;
import com.flydeer.structmind.common.exception.ErrorCodes;
import com.flydeer.structmind.common.exception.business.BusinessException;
import com.flydeer.structmind.contract.base.response.ApiResult;
import com.flydeer.structmind.contract.user.vo.AuthorizeUrlResponse;
import com.flydeer.structmind.contract.user.request.SmsLoginRequest;
import com.flydeer.structmind.contract.user.request.SmsSendRequest;
import com.flydeer.structmind.contract.user.vo.TokenResponse;
import com.flydeer.structmind.contract.user.enums.LoginChannel;
import com.flydeer.structmind.controller.support.AuthCookieSupport;
import com.flydeer.structmind.service.user.utils.JwtTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthorizationApiImpl authServiceImpl;
    private final AuthCookieSupport authCookieSupport;
    private final AppAuthProperties properties;

    public AuthController(
        AuthorizationApiImpl authServiceImpl, AuthCookieSupport authCookieSupport, AppAuthProperties properties) {
        this.authServiceImpl = authServiceImpl;
        this.authCookieSupport = authCookieSupport;
        this.properties = properties;
    }

    @PostMapping("/sms/send")
    public ApiResult<Void> sendSms(@Valid @RequestBody SmsSendRequest request, HttpServletRequest http) {
        authServiceImpl.sendSmsCode(request.getPhone(), clientIp(http));
        return ApiResult.ok();
    }

    @PostMapping("/sms/login")
    public ApiResult<TokenResponse> smsLogin(
            @Valid @RequestBody SmsLoginRequest request,
            HttpServletRequest http,
            HttpServletResponse response) {
        JwtTokenUtils.IssuedTokens tokens =
                authServiceImpl.loginBySms(request.getPhone(), request.getCode(), clientIp(http));
        authCookieSupport.writeRefreshCookie(response, tokens);
        return ApiResult.ok(authServiceImpl.toTokenResponse(tokens));
    }

    @GetMapping("/{provider}/authorize")
    public ApiResult<AuthorizeUrlResponse> authorize(@PathVariable("provider") String provider) {
        return ApiResult.ok(authServiceImpl.authorizeUrl(parseProvider(provider)));
    }

    @GetMapping("/{provider}/callback")
    public void callback(
            @PathVariable("provider") String provider,
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response)
            throws IOException {
        JwtTokenUtils.IssuedTokens tokens =
                authServiceImpl.oauthCallback(parseProvider(provider), code, state);
        authCookieSupport.writeRefreshCookie(response, tokens);
        String redirect = properties.getAuth().getFrontendRedirectUrl()
                + (properties.getAuth().getFrontendRedirectUrl().contains("?") ? "&" : "?")
                + "accessToken="
                + URLEncoder.encode(tokens.accessToken(), StandardCharsets.UTF_8);
        response.sendRedirect(redirect);
    }

    @PostMapping("/refresh")
    public ApiResult<TokenResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refresh = authCookieSupport.readRefreshCookie(request);
        if (!StringUtils.hasText(refresh)) {
            throw new BusinessException(ErrorCodes.UNAUTHORIZED, "refresh token missing");
        }
        JwtTokenUtils.IssuedTokens tokens = authServiceImpl.refresh(refresh);
        authCookieSupport.writeRefreshCookie(response, tokens);
        return ApiResult.ok(authServiceImpl.toTokenResponse(tokens));
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout(HttpServletResponse response) {
        authCookieSupport.clearRefreshCookie(response);
        return ApiResult.ok();
    }

    private LoginChannel parseProvider(String provider) {
        try {
            LoginChannel channel = LoginChannel.valueOf(provider.toUpperCase(Locale.ROOT));
            if (channel == LoginChannel.PHONE) {
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
