package com.flydeer.repository.postgres.repository;

import com.flydeer.repository.postgres.dto.GraphDTO;
import com.flydeer.repository.postgres.dto.GraphParentLink;
import com.flydeer.repository.postgres.entity.GraphEntity;
import com.flydeer.repository.postgres.mapper.GraphMapper;
import com.flydeer.repository.postgres.mapping.GraphMapping;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class GraphRepository {

    private final GraphMapper graphMapper;

    public GraphDTO findByGraphIdIncludingDeleted(String graphId) {
        return GraphMapping.INSTANCE.toDto(graphMapper.selectByGraphId(graphId));
    }

    public GraphDTO findActiveByGraphId(String graphId) {
        return GraphMapping.INSTANCE.toDto(graphMapper.selectActiveByGraphId(graphId));
    }

    public List<GraphDTO> listMetaByAtlasId(Long atlasId, String keyword) {
        return GraphMapping.INSTANCE.toDtoList(graphMapper.selectMetaByAtlasId(atlasId, keyword));
    }

    public List<GraphDTO> listContentByAtlasId(Long atlasId) {
        return GraphMapping.INSTANCE.toDtoList(graphMapper.selectContentByAtlasId(atlasId));
    }

    public List<GraphParentLink> listParentLinks(Long atlasId) {
        return graphMapper.selectParentLinksByAtlasId(atlasId);
    }

    public void insert(GraphDTO dto) throws DuplicateKeyException {
        GraphEntity entity = GraphMapping.INSTANCE.dto2entity(dto);
        graphMapper.insert(entity);
    }

    public boolean updateContent(String graphId, String name, String contentJson, int expectedRev, int newRev,
        long updatedAt) {
        return graphMapper.updateContent(graphId, name, contentJson, expectedRev, newRev, updatedAt) > 0;
    }

    public boolean updateName(String graphId, String name, long updatedAt) {
        return graphMapper.updateName(graphId, name, updatedAt) > 0;
    }

    public boolean updateParent(String graphId, String parentGraphId, long updatedAt) {
        return graphMapper.updateParent(graphId, parentGraphId, updatedAt) > 0;
    }

    public int logicalDeleteByIds(List<String> graphIds, long updatedAt) {
        if (graphIds == null || graphIds.isEmpty()) {
            return 0;
        }
        return graphMapper.logicalDeleteByIds(graphIds, updatedAt);
    }
}
