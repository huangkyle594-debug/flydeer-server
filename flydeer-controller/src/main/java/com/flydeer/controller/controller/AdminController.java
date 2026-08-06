package com.flydeer.controller.controller;

import com.flydeer.common.enums.AuthRequiredLevel;
import com.flydeer.common.enums.AuthResolveLevel;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.contract.admin.AdminApi;
import com.flydeer.contract.admin.request.DisableUserRequest;
import com.flydeer.contract.common.request.ApiRequest;
import com.flydeer.contract.common.response.ApiResult;
import com.flydeer.controller.aop.AuthCheck;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminApi adminApi;

    @PostMapping("/user/disable")
    public ApiResult<Void> disableUser(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.ADMIN) ApiRequest apiRequest,
        @RequestBody DisableUserRequest body)
        throws UserNotFoundException {

        DisableUserRequest request = new DisableUserRequest(apiRequest);
        request.setOperatorId(body.getOperatorId());
        adminApi.disableUser(request);
        return ApiResult.ok();
    }
}
