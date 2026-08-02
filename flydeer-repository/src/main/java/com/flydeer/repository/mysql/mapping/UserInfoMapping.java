package com.flydeer.repository.mysql.mapping;

import com.flydeer.repository.mysql.dto.UserInfoDTO;
import com.flydeer.repository.mysql.entity.UserInfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserInfoMapping {

    UserInfoMapping INSTANCE = Mappers.getMapper(UserInfoMapping.class);

    @Mapping(target = "delegatorIds", expression = "java(new java.util.ArrayList<>())")
    UserInfoDTO toDto(UserInfoEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserInfoEntity dto2entity(UserInfoDTO dto);
}
