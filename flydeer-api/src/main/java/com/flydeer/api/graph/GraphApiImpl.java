package com.flydeer.api.graph;

import com.flydeer.api.graph.mapper.GraphVoMapper;
import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.business.AtlasNotVisibleException;
import com.flydeer.common.exception.business.GraphNotFoundException;
import com.flydeer.common.exception.request.*;
import com.flydeer.contract.graph.GraphApi;
import com.flydeer.contract.graph.request.*;
import com.flydeer.contract.graph.vo.*;
import com.flydeer.repository.postgres.dto.GraphDTO;
import com.flydeer.service.graph.GraphService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GraphApiImpl implements GraphApi {

    private final GraphService graphService;

    @Override
    public List<GraphMetaVO> list(@Valid GraphListRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, AtlasNotPublishedException {
        return GraphVoMapper.toMetaVOList(graphService.list(request));
    }

    @Override
    public GraphVO detail(@Valid GraphIdRequest request)
        throws GraphNotFoundException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException,
        AtlasNotPublishedException {
        return GraphVoMapper.toVO(graphService.detail(request.getGraphId(), request.getAllUserIds()));
    }

    @Override
    public GraphMetaVO save(@Valid GraphSaveRequest request)
        throws BadRequestException,
        GraphRevConflictException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {
        return GraphVoMapper.toMetaVO(graphService.save(request));
    }

    @Override
    public GraphBatchSaveVO batchSave(@Valid GraphBatchSaveRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, BadRequestException {
        List<GraphSaveResultVO> results = graphService.batchSave(request);
        int okCount = (int) results.stream().filter(GraphSaveResultVO::ok).count();
        return new GraphBatchSaveVO(okCount, results);
    }

    @Override
    public GraphMetaVO rename(@Valid GraphRenameRequest request)
        throws GraphNotFoundException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {
        return GraphVoMapper.toMetaVO(graphService.rename(request));
    }

    @Override
    public GraphMetaVO move(@Valid GraphMoveRequest request)
        throws GraphNotFoundException, GraphParentInvalidException, AtlasNotFoundException, AtlasForbiddenException,
        AtlasNotVisibleException {
        return GraphVoMapper.toMetaVO(graphService.move(request));
    }

    @Override
    public GraphDeleteVO delete(@Valid GraphIdRequest request)
        throws GraphNotFoundException, AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {
        return new GraphDeleteVO(graphService.delete(request.getGraphId(), request.getAllUserIds()));
    }

    @Override
    public List<GraphVO> listContent(@Valid GraphListRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, AtlasNotPublishedException,
        GraphContentTooLargeException {
        List<GraphDTO> rows = graphService.listContent(request);
        return GraphVoMapper.toVOList(rows);
    }
}
