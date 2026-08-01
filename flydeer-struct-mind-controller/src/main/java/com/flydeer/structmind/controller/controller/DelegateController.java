package com.flydeer.structmind.controller.controller;

import com.flydeer.structmind.api.user.UserDelegateApiImpl;
import com.flydeer.structmind.common.enums.AuthRequiredLevel;
import com.flydeer.structmind.common.enums.AuthResolveLevel;
import com.flydeer.structmind.common.exception.business.DelegateNotFoundException;
import com.flydeer.structmind.common.exception.business.UserInvalidException;
import com.flydeer.structmind.common.exception.business.UserNotFoundException;
import com.flydeer.structmind.common.exception.request.BadRequestException;
import com.flydeer.structmind.common.exception.request.DelegateSelfException;
import com.flydeer.structmind.contract.base.request.ApiRequest;
import com.flydeer.structmind.contract.base.response.ApiResult;
import com.flydeer.structmind.contract.user.request.DelegateOperateRequest;
import com.flydeer.structmind.contract.user.request.QueryDelegateRequest;
import com.flydeer.structmind.contract.user.vo.DelegateVO;
import com.flydeer.structmind.controller.aop.AuthCheck;
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

    private final UserDelegateApiImpl userDelegateApi;

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
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.AUTHENTICATED) ApiRequest apiRequest,
        @RequestBody DelegateOperateRequest body)
        throws UserNotFoundException, UserInvalidException, DelegateSelfException {

        DelegateOperateRequest request = new DelegateOperateRequest(apiRequest);
        request.setOperateId(body.getOperateId());
        userDelegateApi.delegate(request);
        return ApiResult.ok();
    }

    @PostMapping("/accept")
    public ApiResult<Void> accept(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.AUTHENTICATED) ApiRequest apiRequest,
        @RequestBody DelegateOperateRequest body)
        throws UserNotFoundException, UserInvalidException, DelegateNotFoundException {

        DelegateOperateRequest request = new DelegateOperateRequest(apiRequest);
        request.setOperateId(body.getOperateId());
        userDelegateApi.accept(request);
        return ApiResult.ok();
    }

    @PostMapping("/revoke")
    public ApiResult<Void> revoke(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.AUTHENTICATED) ApiRequest apiRequest,
        @RequestBody DelegateOperateRequest body)
        throws DelegateNotFoundException, BadRequestException {

        DelegateOperateRequest request = new DelegateOperateRequest(apiRequest);
        request.setOperateId(body.getOperateId());
        request.setRelation(body.getRelation());
        userDelegateApi.revoke(request);
        return ApiResult.ok();
    }
}
