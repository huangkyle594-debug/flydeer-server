package com.flydeer.structmind.controller.web;

import com.flydeer.structmind.api.user.UserFacade;
import com.flydeer.structmind.common.result.ApiResult;
import com.flydeer.structmind.contract.auth.BaseRequest;
import com.flydeer.structmind.contract.auth.TokenResponse;
import com.flydeer.structmind.contract.enums.UserLevel;
import com.flydeer.structmind.contract.user.BindPhoneRequest;
import com.flydeer.structmind.contract.user.UpdateNicknameRequest;
import com.flydeer.structmind.contract.user.UserProfileResponse;
import com.flydeer.structmind.controller.auth.RequireUserLevel;
import com.flydeer.structmind.controller.support.AuthCookieSupport;
import com.flydeer.structmind.service.auth.JwtTokenService;
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

    private final UserFacade userFacade;
    private final AuthCookieSupport authCookieSupport;

    public UserController(UserFacade userFacade, AuthCookieSupport authCookieSupport) {
        this.userFacade = userFacade;
        this.authCookieSupport = authCookieSupport;
    }

    @GetMapping("/me")
    @RequireUserLevel(UserLevel.AUTHENTICATED)
    public ApiResult<UserProfileResponse> me(BaseRequest request) {
        return ApiResult.ok(userFacade.me(request.getUserId()));
    }

    @PatchMapping("/me")
    @RequireUserLevel(UserLevel.AUTHENTICATED)
    public ApiResult<UserProfileResponse> updateNickname(@Valid @RequestBody UpdateNicknameRequest request) {
        return ApiResult.ok(userFacade.updateNickname(request.getUserId(), request.getNickName()));
    }

    @PostMapping("/me/phone/bind")
    @RequireUserLevel(UserLevel.AUTHENTICATED)
    public ApiResult<TokenResponse> bindPhone(
            @Valid @RequestBody BindPhoneRequest request, HttpServletResponse response) {
        JwtTokenService.IssuedTokens tokens =
                userFacade.bindPhone(request.getUserId(), request.getPhone(), request.getCode());
        authCookieSupport.writeRefreshCookie(response, tokens);
        return ApiResult.ok(userFacade.toTokenResponse(tokens));
    }
}
