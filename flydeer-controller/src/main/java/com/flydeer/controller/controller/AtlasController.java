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
import com.flydeer.contract.atlas.request.AtlasImportRequest;
import com.flydeer.contract.atlas.request.AtlasQueryRequest;
import com.flydeer.contract.atlas.request.AtlasUpdateRequest;
import com.flydeer.contract.atlas.vo.AtlasPageVO;
import com.flydeer.contract.atlas.vo.AtlasVO;
import com.flydeer.contract.base.request.ApiRequest;
import com.flydeer.contract.base.response.ApiResult;
import com.flydeer.controller.aop.AuthCheck;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/struct-mind/v1")
public class AtlasController {

    private final AtlasApi atlasApi;

    @PostMapping("/atlases/query")
    public ApiResult<AtlasPageVO> list(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.ANONYMOUS) ApiRequest apiRequest,
        @RequestBody(required = false) AtlasQueryRequest body)
        throws NeedLoginException {

        AtlasQueryRequest request = new AtlasQueryRequest(apiRequest);
        if (body != null) {
            request.setKeyword(body.getKeyword());
            request.setEditable(body.getEditable());
            request.setTags(body.getTags());
            request.setPage(body.getPage());
            request.setPageSize(body.getPageSize());
        }
        return ApiResult.ok(atlasApi.listAtlases(request));
    }

    @PostMapping("/tags/query")
    public ApiResult<List<String>> tags(
        @AuthCheck(resolve = AuthResolveLevel.NONE, required = AuthRequiredLevel.ANONYMOUS) ApiRequest apiRequest) {
        return ApiResult.ok(atlasApi.listTags());
    }

    @PostMapping("/atlases/create")
    public ApiResult<AtlasVO> create(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.AUTHENTICATED) ApiRequest apiRequest,
        @RequestBody AtlasCreateRequest body)
        throws UserNotFoundException, UserInvalidException, BadRequestException {

        AtlasCreateRequest request = new AtlasCreateRequest(apiRequest);
        request.setName(body.getName());
        request.setDescription(body.getDescription());
        request.setTags(body.getTags());
        return ApiResult.ok(atlasApi.createAtlas(request));
    }

    @PostMapping("/atlases/update")
    public ApiResult<AtlasVO> update(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.AUTHENTICATED) ApiRequest apiRequest,
        @RequestBody AtlasUpdateRequest body)
        throws AtlasNotFoundException, AtlasForbiddenException, BadRequestException {

        AtlasUpdateRequest request = new AtlasUpdateRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        request.setName(body.getName());
        request.setDescription(body.getDescription());
        request.setTags(body.getTags());
        return ApiResult.ok(atlasApi.updateAtlas(request));
    }

    @PostMapping("/atlases/submit-review")
    public ApiResult<Void> submitReview(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.AUTHENTICATED) ApiRequest apiRequest,
        @RequestBody AtlasIdRequest body)
        throws AtlasNotFoundException, AtlasForbiddenException, BadRequestException {

        AtlasIdRequest request = new AtlasIdRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        atlasApi.submitReview(request);
        return ApiResult.ok();
    }

    @PostMapping("/atlases/delete")
    public ApiResult<Void> delete(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.AUTHENTICATED) ApiRequest apiRequest,
        @RequestBody AtlasIdRequest body)
        throws AtlasNotFoundException, AtlasForbiddenException {

        AtlasIdRequest request = new AtlasIdRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        atlasApi.deleteAtlas(request);
        return ApiResult.ok();
    }

    @PostMapping("/atlases/import")
    public ApiResult<AtlasVO> importAtlas(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.AUTHENTICATED) ApiRequest apiRequest,
        @RequestBody AtlasImportRequest body)
        throws UserNotFoundException, UserInvalidException, BadRequestException {

        AtlasImportRequest request = new AtlasImportRequest(apiRequest);
        request.setFormat(body.getFormat());
        request.setVersion(body.getVersion());
        request.setAtlas(body.getAtlas());
        request.setGraphs(body.getGraphs());
        return ApiResult.ok(atlasApi.importAtlas(request));
    }

    @PostMapping("/atlases/detail")
    public ApiResult<AtlasVO> detail(
        @AuthCheck(resolve = AuthResolveLevel.SELF, required = AuthRequiredLevel.ANONYMOUS) ApiRequest apiRequest,
        @RequestBody AtlasIdRequest body)
        throws AtlasNotFoundException, AtlasForbiddenException {

        AtlasIdRequest request = new AtlasIdRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        return ApiResult.ok(atlasApi.getAtlas(request));
    }
}
