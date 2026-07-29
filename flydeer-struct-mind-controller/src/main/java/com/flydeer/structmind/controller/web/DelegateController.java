package com.flydeer.structmind.controller.web;

import com.flydeer.structmind.api.delegate.DelegateFacade;
import com.flydeer.structmind.common.result.ApiResult;
import com.flydeer.structmind.contract.auth.BaseRequest;
import com.flydeer.structmind.contract.delegate.CreateDelegateRequest;
import com.flydeer.structmind.contract.delegate.DelegateItemResponse;
import com.flydeer.structmind.contract.enums.UserLevel;
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
@RequireUserLevel(UserLevel.AUTHENTICATED)
public class DelegateController {

    private final DelegateFacade delegateFacade;

    public DelegateController(DelegateFacade delegateFacade) {
        this.delegateFacade = delegateFacade;
    }

    @PostMapping
    public ApiResult<Void> create(@Valid @RequestBody CreateDelegateRequest request) {
        delegateFacade.create(request.getUserId(), request.getPeerUserId(), request.getRequestType());
        return ApiResult.ok();
    }

    @GetMapping
    public ApiResult<List<DelegateItemResponse>> list(BaseRequest request) {
        return ApiResult.ok(delegateFacade.list(request.getUserId()));
    }

    @PostMapping("/{peerUserId}/accept")
    public ApiResult<Void> accept(@PathVariable Long peerUserId, BaseRequest request) {
        delegateFacade.accept(request.getUserId(), peerUserId);
        return ApiResult.ok();
    }

    @PostMapping("/{peerUserId}/reject")
    public ApiResult<Void> reject(@PathVariable Long peerUserId, BaseRequest request) {
        delegateFacade.reject(request.getUserId(), peerUserId);
        return ApiResult.ok();
    }

    @PostMapping("/{peerUserId}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long peerUserId, BaseRequest request) {
        delegateFacade.cancel(request.getUserId(), peerUserId);
        return ApiResult.ok();
    }
}
