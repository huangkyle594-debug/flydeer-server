package com.flydeer.contract.graph;

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

import java.util.List;

public interface GraphApi {

    List<GraphMetaVO> list(GraphListRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, AtlasNotPublishedException;

    GraphVO detail(GraphIdRequest request)
        throws GraphNotFoundException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException,
        AtlasNotPublishedException;

    GraphMetaVO save(GraphSaveRequest request)
        throws BadRequestException,
        GraphRevConflictException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException;

    GraphBatchSaveVO batchSave(GraphBatchSaveRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, BadRequestException;

    GraphMetaVO rename(GraphRenameRequest request)
        throws GraphNotFoundException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException;

    GraphMetaVO move(GraphMoveRequest request)
        throws GraphNotFoundException, GraphParentInvalidException, AtlasNotFoundException, AtlasForbiddenException,
        AtlasNotVisibleException;

    GraphDeleteVO delete(GraphIdRequest request)
        throws GraphNotFoundException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException;

    List<GraphVO> listContent(GraphListRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, AtlasNotPublishedException,
        GraphContentTooLargeException;
}
