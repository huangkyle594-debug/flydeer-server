package com.flydeer.structmind.controller.web;

import com.flydeer.structmind.api.user.UserDelegateApiImpl;
import com.flydeer.structmind.contract.base.response.ApiResult;
import com.flydeer.structmind.contract.base.request.ApiRequest;
import com.flydeer.structmind.contract.user.request.CreateDelegateRequest;
import com.flydeer.structmind.contract.user.vo.DelegateVO;
import com.flydeer.structmind.contract.user.enums.UserLevelEnum;
import com.flydeer.structmind.controller.auth.RequireUserLevel;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/delegates")
@RequireUserLevel(UserLevelEnum.AUTHENTICATED)
public class DelegateController {

    private final UserDelegateApiImpl userDelegateApiImpl;

    public DelegateController(UserDelegateApiImpl userDelegateApiImpl) {
        this.userDelegateApiImpl = userDelegateApiImpl;
    }

    @PostMapping
    public ApiResult<Void> create(@Valid @RequestBody CreateDelegateRequest request) {
        userDelegateApiImpl.create(request.getUserId(), request.getPeerUserId(), request.getRequestType());
        return ApiResult.ok();
    }

    @GetMapping
    public ApiResult<List<DelegateVO>> list(ApiRequest request) {
        return ApiResult.ok(userDelegateApiImpl.list(request.getUserId()));
    }

    @PostMapping("/{peerUserId}/accept")
    public ApiResult<Void> accept(@PathVariable Long peerUserId, ApiRequest request) {
        userDelegateApiImpl.accept(request.getUserId(), peerUserId);
        return ApiResult.ok();
    }

    @PostMapping("/{peerUserId}/reject")
    public ApiResult<Void> reject(@PathVariable Long peerUserId, ApiRequest request) {
        userDelegateApiImpl.reject(request.getUserId(), peerUserId);
        return ApiResult.ok();
    }

    @PostMapping("/{peerUserId}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long peerUserId, ApiRequest request) {
        userDelegateApiImpl.cancel(request.getUserId(), peerUserId);
        return ApiResult.ok();
    }
}
