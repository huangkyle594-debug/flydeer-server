package com.flydeer.controller.controller;

import com.flydeer.common.enums.AuthRequiredLevel;
import com.flydeer.common.enums.AuthResolveLevel;
import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.business.AtlasNotVisibleException;
import com.flydeer.common.exception.business.GraphNotFoundException;
import com.flydeer.common.exception.request.AtlasNotPublishedException;
import com.flydeer.common.exception.request.BadRequestException;
import com.flydeer.common.exception.request.GraphContentTooLargeException;
import com.flydeer.common.exception.request.GraphIdInvalidException;
import com.flydeer.common.exception.request.GraphParentInvalidException;
import com.flydeer.common.exception.request.GraphRevConflictException;
import com.flydeer.contract.common.request.ApiRequest;
import com.flydeer.contract.common.response.ApiResult;
import com.flydeer.contract.graph.GraphApi;
import com.flydeer.contract.graph.request.GraphBatchSaveRequest;
import com.flydeer.contract.graph.request.GraphIdRequest;
import com.flydeer.contract.graph.request.GraphListRequest;
import com.flydeer.contract.graph.request.GraphMoveRequest;
import com.flydeer.contract.graph.request.GraphRenameRequest;
import com.flydeer.contract.graph.request.GraphSaveRequest;
import com.flydeer.contract.graph.vo.GraphBatchSaveVO;
import com.flydeer.contract.graph.vo.GraphDeleteVO;
import com.flydeer.contract.graph.vo.GraphMetaVO;
import com.flydeer.contract.graph.vo.GraphVO;
import com.flydeer.controller.aop.AuthCheck;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/struct-mind/graphs")
public class GraphController {

    private final GraphApi graphApi;

    @PostMapping("/list")
    public ApiResult<List<GraphMetaVO>> list(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE) ApiRequest apiRequest,
        @RequestBody GraphListRequest body)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, AtlasNotPublishedException {

        GraphListRequest request = new GraphListRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        request.setKeyword(body.getKeyword());
        return ApiResult.ok(graphApi.list(request));
    }

    @PostMapping("/detail")
    public ApiResult<GraphVO> detail(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE) ApiRequest apiRequest,
        @RequestBody GraphIdRequest body)
        throws GraphNotFoundException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException,
        AtlasNotPublishedException {

        GraphIdRequest request = new GraphIdRequest(apiRequest);
        request.setGraphId(body.getGraphId());
        return ApiResult.ok(graphApi.detail(request));
    }

    @PostMapping("/save")
    public ApiResult<GraphMetaVO> save(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody GraphSaveRequest body)
        throws GraphIdInvalidException, GraphContentTooLargeException, GraphParentInvalidException,
        GraphRevConflictException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException,
        BadRequestException {

        GraphSaveRequest request = new GraphSaveRequest(apiRequest);
        request.setGraphId(body.getGraphId());
        request.setAtlasId(body.getAtlasId());
        request.setName(body.getName());
        request.setParentGraphId(body.getParentGraphId());
        request.setRev(body.getRev());
        request.setContent(body.getContent());
        return ApiResult.ok(graphApi.save(request));
    }

    @PostMapping("/batch-save")
    public ApiResult<GraphBatchSaveVO> batchSave(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody GraphBatchSaveRequest body)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, BadRequestException,
        GraphContentTooLargeException {

        GraphBatchSaveRequest request = new GraphBatchSaveRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        request.setGraphs(body.getGraphs());
        return ApiResult.ok(graphApi.batchSave(request));
    }

    @PostMapping("/rename")
    public ApiResult<GraphMetaVO> rename(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody GraphRenameRequest body)
        throws GraphNotFoundException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {

        GraphRenameRequest request = new GraphRenameRequest(apiRequest);
        request.setGraphId(body.getGraphId());
        request.setName(body.getName());
        return ApiResult.ok(graphApi.rename(request));
    }

    @PostMapping("/move")
    public ApiResult<GraphMetaVO> move(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody GraphMoveRequest body)
        throws GraphNotFoundException, GraphParentInvalidException, AtlasNotFoundException, AtlasForbiddenException,
        AtlasNotVisibleException {

        GraphMoveRequest request = new GraphMoveRequest(apiRequest);
        request.setGraphId(body.getGraphId());
        request.setParentGraphId(body.getParentGraphId());
        return ApiResult.ok(graphApi.move(request));
    }

    @PostMapping("/delete")
    public ApiResult<GraphDeleteVO> delete(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE, required = AuthRequiredLevel.VERIFIED) ApiRequest apiRequest,
        @RequestBody GraphIdRequest body)
        throws GraphNotFoundException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {

        GraphIdRequest request = new GraphIdRequest(apiRequest);
        request.setGraphId(body.getGraphId());
        return ApiResult.ok(graphApi.delete(request));
    }

    @PostMapping("/list-content")
    public ApiResult<List<GraphVO>> listContent(
        @AuthCheck(resolve = AuthResolveLevel.DELEGATE) ApiRequest apiRequest,
        @RequestBody GraphListRequest body)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, AtlasNotPublishedException,
        GraphContentTooLargeException {

        GraphListRequest request = new GraphListRequest(apiRequest);
        request.setAtlasId(body.getAtlasId());
        return ApiResult.ok(graphApi.listContent(request));
    }
}
