package com.flydeer.repository.mysql.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flydeer.repository.mysql.dto.AtlasDTO;
import com.flydeer.repository.mysql.entity.AtlasEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

@Mapper
public interface AtlasMapping {

    AtlasMapping INSTANCE = Mappers.getMapper(AtlasMapping.class);

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "tags", source = "tags", qualifiedByName = "jsonToTags")
    @Mapping(target = "visible", source = "visible", qualifiedByName = "intToBool")
    AtlasDTO toDto(AtlasEntity entity);

    List<AtlasDTO> toDtoList(List<AtlasEntity> entities);

    @Mapping(target = "tags", source = "tags", qualifiedByName = "tagsToJson")
    @Mapping(target = "visible", source = "visible", qualifiedByName = "boolToInt")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AtlasEntity dto2entity(AtlasDTO dto);

    @Named("jsonToTags")
    default List<String> jsonToTags(String tagsJson) {
        if (tagsJson == null || tagsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(tagsJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    @Named("tagsToJson")
    default String tagsToJson(List<String> tags) {
        if (tags == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    @Named("intToBool")
    default Boolean intToBool(Integer value) {
        return value != null && value != 0;
    }

    @Named("boolToInt")
    default Integer boolToInt(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? 1 : 0;
    }
}
