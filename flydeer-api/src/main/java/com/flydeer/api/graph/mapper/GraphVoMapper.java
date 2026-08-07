package com.flydeer.api.graph.mapper;

import com.flydeer.contract.graph.vo.GraphMetaVO;
import com.flydeer.contract.graph.vo.GraphVO;
import com.flydeer.repository.postgres.dto.GraphDTO;

import java.util.List;

public final class GraphVoMapper {

    private GraphVoMapper() {
    }

    public static GraphMetaVO toMetaVO(GraphDTO dto) {
        if (dto == null) {
            return null;
        }
        return new GraphMetaVO(
            dto.getGraphId(),
            dto.getAtlasId(),
            dto.getName(),
            dto.getParentGraphId(),
            dto.getNodeCount() == null ? 0 : dto.getNodeCount(),
            dto.getRev(),
            dto.getCreatedAt(),
            dto.getUpdatedAt());
    }

    public static GraphVO toVO(GraphDTO dto) {
        if (dto == null) {
            return null;
        }
        return new GraphVO(
            dto.getGraphId(),
            dto.getAtlasId(),
            dto.getName(),
            dto.getParentGraphId(),
            dto.getNodeCount() == null ? 0 : dto.getNodeCount(),
            dto.getRev(),
            dto.getCreatedAt(),
            dto.getUpdatedAt(),
            dto.getContent());
    }

    public static List<GraphMetaVO> toMetaVOList(List<GraphDTO> rows) {
        return rows.stream().map(GraphVoMapper::toMetaVO).toList();
    }

    public static List<GraphVO> toVOList(List<GraphDTO> rows) {
        return rows.stream().map(GraphVoMapper::toVO).toList();
    }
}
