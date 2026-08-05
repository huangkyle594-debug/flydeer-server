package com.flydeer.controller.controller;

import com.flydeer.common.enums.AuthRequiredLevel;
import com.flydeer.common.enums.AuthResolveLevel;
import com.flydeer.common.exception.auth.NeedLoginException;
import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.exception.request.BadRequestException;
import com.flydeer.contract.atlas.AtlasApi;
import com.flydeer.contract.atlas.request.AtlasCreateRequest;
import com.flydeer.contract.atlas.request.AtlasIdRequest;
import com.flydeer.contract.atlas.request.AtlasQueryRequest;
import com.flydeer.contract.atlas.request.AtlasUpdateRequest;
import com.flydeer.contract.atlas.vo.AtlasPageVO;
import com.flydeer.contract.atlas.vo.AtlasVO;
import com.flydeer.contract.common.request.ApiRequest;
import com.flydeer.contract.common.response.ApiResult;
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
    public ApiResult<AtlasPageVO> list(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE) ApiRequest apiRequest,
        @RequestBody(required = false) AtlasQueryRequest body)
        throws NeedLoginException {

        AtlasQueryRequest request = new AtlasQueryRequest(apiRequest);
        if (body != null) {
            request.setKeyword(body.getKeyword());
            request.setScope(body.getScope());
            request.setTags(body.getTags());
            request.setPage(body.getPage());
            request.setPageSize(body.getPageSize());
        }
        return ApiResult.ok(atlasApi.listAtlases(request));
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
        throws AtlasNotFoundException, AtlasForbiddenException, BadRequestException {

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
        throws AtlasNotFoundException, AtlasForbiddenException, BadRequestException {

        AtlasIdRequest request = new AtlasIdRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        atlasApi.submitReview(request);
        return ApiResult.ok();
    }

    @PostMapping("/delete")
    public ApiResult<Void> delete(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody AtlasIdRequest body)
        throws AtlasNotFoundException, AtlasForbiddenException {

        AtlasIdRequest request = new AtlasIdRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        atlasApi.deleteAtlas(request);
        return ApiResult.ok();
    }

    @PostMapping("/detail")
    public ApiResult<AtlasVO> detail(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.ANONYMOUS) ApiRequest apiRequest,
        @RequestBody AtlasIdRequest body)
        throws AtlasNotFoundException, AtlasForbiddenException {

        AtlasIdRequest request = new AtlasIdRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        return ApiResult.ok(atlasApi.getAtlas(request));
    }
}
