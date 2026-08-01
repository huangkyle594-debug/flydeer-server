package com.flydeer.structmind.controller.web;

import com.flydeer.structmind.api.user.UserMangeApiImpl;
import com.flydeer.structmind.contract.base.response.ApiResult;
import com.flydeer.structmind.contract.base.request.ApiRequest;
import com.flydeer.structmind.contract.user.vo.TokenResponse;
import com.flydeer.structmind.contract.user.enums.UserLevelEnum;
import com.flydeer.structmind.contract.user.request.BindPhoneRequest;
import com.flydeer.structmind.contract.user.vo.UserProfileVO;
import com.flydeer.structmind.controller.auth.RequireUserLevel;
import com.flydeer.structmind.controller.support.AuthCookieSupport;
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
    private final AuthCookieSupport authCookieSupport;

    public UserController(UserMangeApiImpl userApiImpl, AuthCookieSupport authCookieSupport) {
        this.userApiImpl = userApiImpl;
        this.authCookieSupport = authCookieSupport;
    }

    @GetMapping("/me")
    @RequireUserLevel(UserLevelEnum.AUTHENTICATED)
    public ApiResult<UserProfileVO> me(ApiRequest request) {
        return ApiResult.ok(userApiImpl.me(request.getUserId()));
    }

    @PatchMapping("/me")
    @RequireUserLevel(UserLevelEnum.AUTHENTICATED)
    public ApiResult<UserProfileVO> updateNickname(@Valid @RequestBody UpdateNicknameRequest request) {
        return ApiResult.ok(userApiImpl.update(request.getUserId(), request.getNickName()));
    }

    @PostMapping("/me/phone/bind")
    @RequireUserLevel(UserLevelEnum.AUTHENTICATED)
    public ApiResult<TokenResponse> bindPhone(
            @Valid @RequestBody BindPhoneRequest request, HttpServletResponse response) {
        JwtTokenUtils.IssuedTokens tokens =
                userApiImpl.bindPhone(request.getUserId(), request.getPhone(), request.getCode());
        authCookieSupport.writeRefreshCookie(response, tokens);
        return ApiResult.ok(userApiImpl.toTokenResponse(tokens));
    }
}
