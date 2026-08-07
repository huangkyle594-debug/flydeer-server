package com.flydeer.repository.postgres.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flydeer.repository.postgres.dto.GraphDTO;
import com.flydeer.repository.postgres.entity.GraphEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface GraphMapping {

    GraphMapping INSTANCE = Mappers.getMapper(GraphMapping.class);

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "content", source = "content", qualifiedByName = "jsonToNode")
    GraphDTO toDto(GraphEntity entity);

    List<GraphDTO> toDtoList(List<GraphEntity> entities);

    @Mapping(target = "content", source = "content", qualifiedByName = "nodeToJson")
    @Mapping(target = "nodeCount", ignore = true)
    GraphEntity dto2entity(GraphDTO dto);

    @Named("jsonToNode")
    default JsonNode jsonToNode(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("invalid graph content json", e);
        }
    }

    @Named("nodeToJson")
    default String nodeToJson(JsonNode node) {
        if (node == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize graph content", e);
        }
    }
}
