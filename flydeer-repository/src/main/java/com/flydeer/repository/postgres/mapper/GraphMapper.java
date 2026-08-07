package com.flydeer.repository.postgres.mapper;

import com.flydeer.repository.postgres.dto.GraphParentLink;
import com.flydeer.repository.postgres.entity.GraphEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GraphMapper {

    GraphEntity selectByGraphId(@Param("graphId") String graphId);

    GraphEntity selectActiveByGraphId(@Param("graphId") String graphId);

    List<GraphEntity> selectMetaByAtlasId(
        @Param("atlasId") Long atlasId, @Param("keyword") String keyword);

    List<GraphEntity> selectContentByAtlasId(@Param("atlasId") Long atlasId);

    List<GraphParentLink> selectParentLinksByAtlasId(@Param("atlasId") Long atlasId);

    int insert(GraphEntity entity);

    int updateContent(
        @Param("graphId") String graphId,
        @Param("name") String name,
        @Param("content") String content,
        @Param("expectedRev") int expectedRev,
        @Param("newRev") int newRev,
        @Param("updatedAt") long updatedAt);

    int updateName(
        @Param("graphId") String graphId,
        @Param("name") String name,
        @Param("updatedAt") long updatedAt);

    int updateParent(
        @Param("graphId") String graphId,
        @Param("parentGraphId") String parentGraphId,
        @Param("updatedAt") long updatedAt);

    int logicalDeleteByIds(
        @Param("graphIds") List<String> graphIds, @Param("updatedAt") long updatedAt);
}
