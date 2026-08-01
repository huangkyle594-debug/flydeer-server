package com.flydeer.structmind.controller.controller;

import com.flydeer.structmind.api.user.UserMangeApiImpl;
import com.flydeer.structmind.contract.base.response.ApiResult;
import com.flydeer.structmind.contract.base.request.ApiRequest;
import com.flydeer.structmind.contract.user.vo.TokenResponse;
import com.flydeer.structmind.common.enums.AuthRequiredLevel;
import com.flydeer.structmind.contract.user.request.BindPhoneRequest;
import com.flydeer.structmind.contract.user.vo.UserProfileVO;
import com.flydeer.structmind.controller.auth.RequireUserLevel;
import com.flydeer.structmind.controller.support.AuthCookieUtils;
import com.flydeer.structmind.service.user.utils.JwtTokenUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserMangeApiImpl userApiImpl;
    private final AuthCookieUtils authCookieUtils;

    public UserController(UserMangeApiImpl userApiImpl, AuthCookieUtils authCookieUtils) {
        this.userApiImpl = userApiImpl;
        this.authCookieUtils = authCookieUtils;
    }

    @GetMapping("/me")
    @RequireUserLevel(AuthRequiredLevel.AUTHENTICATED)
    public ApiResult<UserProfileVO> me(ApiRequest request) {
        return ApiResult.ok(userApiImpl.me(request.getUserId()));
    }

    @PatchMapping("/me")
    @RequireUserLevel(AuthRequiredLevel.AUTHENTICATED)
    public ApiResult<UserProfileVO> updateNickname(@Valid @RequestBody UpdateNicknameRequest request) {
        return ApiResult.ok(userApiImpl.update(request.getUserId(), request.getNickName()));
    }

    @PostMapping("/me/phone/bind")
    @RequireUserLevel(AuthRequiredLevel.AUTHENTICATED)
    public ApiResult<TokenResponse> bindPhone(
            @Valid @RequestBody BindPhoneRequest request, HttpServletResponse response) {
        JwtTokenUtils.IssuedTokens tokens =
                userApiImpl.bindPhone(request.getUserId(), request.getPhone(), request.getCode());
        authCookieUtils.writeRefreshCookie(response, tokens);
        return ApiResult.ok(userApiImpl.toTokenResponse(tokens));
    }
}
