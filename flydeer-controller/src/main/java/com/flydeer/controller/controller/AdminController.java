package com.flydeer.controller.controller;

import com.flydeer.common.enums.AuthRequiredLevel;
import com.flydeer.common.enums.AuthResolveLevel;
import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.business.AtlasNotVisibleException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.exception.request.AtlasApproveException;
import com.flydeer.contract.admin.AdminApi;
import com.flydeer.contract.admin.request.DisableUserRequest;
import com.flydeer.contract.atlas.request.AtlasIdRequest;
import com.flydeer.contract.atlas.request.AtlasQuery;
import com.flydeer.contract.atlas.vo.AtlasVO;
import com.flydeer.contract.common.request.ApiRequest;
import com.flydeer.contract.common.request.PageRequest;
import com.flydeer.contract.common.response.ApiResult;
import com.flydeer.contract.common.vo.PageVO;
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

    @PostMapping("/atlas/pending")
    public ApiResult<PageVO<AtlasVO>> pagePendingAtlases(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.ADMIN) ApiRequest apiRequest,
        @RequestBody(required = false) PageRequest<AtlasQuery> body) {

        PageRequest<AtlasQuery> request = new PageRequest<>(apiRequest);
        if (body != null) {
            request.setQuery(body.getQuery() != null ? body.getQuery() : new AtlasQuery());
            request.setPage(body.getPage());
            request.setPageSize(body.getPageSize());
            request.setOrderBy(body.getOrderBy());
            request.setIsAsc(body.getIsAsc());
        } else {
            request.setQuery(new AtlasQuery());
        }
        return ApiResult.ok(adminApi.pagePendingAtlases(request));
    }

    @PostMapping("/atlas/approve")
    public ApiResult<Void> approveAtlas(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.ADMIN) ApiRequest apiRequest,
        @RequestBody AtlasIdRequest body)
        throws AtlasNotFoundException, AtlasApproveException, AtlasNotVisibleException, AtlasForbiddenException {

        AtlasIdRequest request = new AtlasIdRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        adminApi.approveAtlas(request);
        return ApiResult.ok();
    }
}
