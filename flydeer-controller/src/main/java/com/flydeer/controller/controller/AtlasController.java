package com.flydeer.controller.controller;

import com.flydeer.common.enums.AuthRequiredLevel;
import com.flydeer.common.enums.AuthResolveLevel;
import com.flydeer.common.exception.auth.NeedLoginException;
import com.flydeer.common.exception.business.*;
import com.flydeer.common.exception.request.AtlasPublishException;
import com.flydeer.contract.atlas.AtlasApi;
import com.flydeer.contract.atlas.request.AtlasCreateRequest;
import com.flydeer.contract.atlas.request.AtlasIdRequest;
import com.flydeer.contract.atlas.request.AtlasQuery;
import com.flydeer.contract.atlas.request.AtlasUpdateRequest;
import com.flydeer.contract.atlas.vo.AtlasVO;
import com.flydeer.contract.common.request.ApiRequest;
import com.flydeer.contract.common.request.PageRequest;
import com.flydeer.contract.common.response.ApiResult;
import com.flydeer.contract.common.vo.PageVO;
import com.flydeer.controller.aop.AuthCheck;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/struct-mind/atlases")
public class AtlasController {

    private final AtlasApi atlasApi;

    @PostMapping("/query")
    public ApiResult<PageVO<AtlasVO>> list(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE) ApiRequest apiRequest,
        @RequestBody(required = false) PageRequest<AtlasQuery> body)
        throws NeedLoginException {

        PageRequest<AtlasQuery> request = new PageRequest<>(apiRequest);
        request.setQuery(body.getQuery());
        request.setPage(body.getPage());
        request.setPageSize(body.getPageSize());
        request.setOrderBy(body.getOrderBy());
        request.setIsAsc(body.getIsAsc());
        return ApiResult.ok(atlasApi.pageQuery(request));
    }

    @GetMapping("/tags")
    public ApiResult<List<String>> tags() {
        return ApiResult.ok(atlasApi.listTags());
    }

    @PostMapping("/create")
    public ApiResult<AtlasVO> create(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody AtlasCreateRequest body)
        throws UserNotFoundException, UserInvalidException {

        AtlasCreateRequest request = new AtlasCreateRequest(apiRequest);
        request.setName(body.getName());
        request.setDescription(body.getDescription());
        request.setTags(body.getTags());
        return ApiResult.ok(atlasApi.createAtlas(request));
    }

    @PostMapping("/update")
    public ApiResult<AtlasVO> update(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody AtlasUpdateRequest body)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {

        AtlasUpdateRequest request = new AtlasUpdateRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        request.setName(body.getName());
        request.setDescription(body.getDescription());
        request.setTags(body.getTags());
        return ApiResult.ok(atlasApi.updateAtlas(request));
    }

    @PostMapping("/submit-review")
    public ApiResult<Void> submitReview(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody AtlasIdRequest body)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, AtlasPublishException {

        AtlasIdRequest request = new AtlasIdRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        atlasApi.submitReview(request);
        return ApiResult.ok();
    }

    @PostMapping("/delete")
    public ApiResult<Void> delete(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody AtlasIdRequest body)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {

        AtlasIdRequest request = new AtlasIdRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        atlasApi.deleteAtlas(request);
        return ApiResult.ok();
    }
}
