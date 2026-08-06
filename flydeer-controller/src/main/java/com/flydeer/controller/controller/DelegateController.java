package com.flydeer.controller.controller;

import com.flydeer.common.enums.AuthRequiredLevel;
import com.flydeer.common.enums.AuthResolveLevel;
import com.flydeer.common.exception.auth.NeedVerifyException;
import com.flydeer.common.exception.business.DelegateNotFoundException;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.exception.request.DelegateRevokeException;
import com.flydeer.common.exception.request.DelegateSelfException;
import com.flydeer.contract.common.request.ApiRequest;
import com.flydeer.contract.common.response.ApiResult;
import com.flydeer.contract.user.UserDelegateApi;
import com.flydeer.contract.user.request.DelegateOperateRequest;
import com.flydeer.contract.user.request.QueryDelegateRequest;
import com.flydeer.contract.user.vo.DelegateVO;
import com.flydeer.controller.aop.AuthCheck;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/user/delegate")
public class DelegateController {

    private final UserDelegateApi userDelegateApi;

    @PostMapping("/query")
    public ApiResult<List<DelegateVO>> list(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.AUTHENTICATED) ApiRequest apiRequest,
        @RequestBody QueryDelegateRequest body) {

        QueryDelegateRequest request = new QueryDelegateRequest(apiRequest);
        request.setStatus(body.getStatus());
        request.setRelation(body.getRelation());
        return ApiResult.ok(userDelegateApi.queryDelegateRelation(request));
    }

    @PostMapping("/create")
    public ApiResult<Void> create(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody DelegateOperateRequest body)
        throws UserNotFoundException, UserInvalidException, DelegateSelfException, NeedVerifyException {

        DelegateOperateRequest request = new DelegateOperateRequest(apiRequest);
        request.setOperateId(body.getOperateId());
        userDelegateApi.delegate(request);
        return ApiResult.ok();
    }

    @PostMapping("/accept")
    public ApiResult<Void> accept(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody DelegateOperateRequest body)
        throws UserNotFoundException, UserInvalidException, DelegateNotFoundException, NeedVerifyException {

        DelegateOperateRequest request = new DelegateOperateRequest(apiRequest);
        request.setOperateId(body.getOperateId());
        userDelegateApi.accept(request);
        return ApiResult.ok();
    }

    @PostMapping("/revoke")
    public ApiResult<Void> revoke(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody DelegateOperateRequest body)
        throws DelegateNotFoundException, DelegateRevokeException {

        DelegateOperateRequest request = new DelegateOperateRequest(apiRequest);
        request.setOperateId(body.getOperateId());
        request.setRelation(body.getRelation());
        userDelegateApi.revoke(request);
        return ApiResult.ok();
    }
}
