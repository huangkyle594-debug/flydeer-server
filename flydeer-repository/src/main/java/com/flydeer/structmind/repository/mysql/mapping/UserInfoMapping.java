package com.flydeer.structmind.repository.mysql.mapping;

import com.flydeer.structmind.repository.mysql.dto.UserInfoDTO;
import com.flydeer.structmind.repository.mysql.entity.UserInfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserInfoMapping {

    UserInfoMapping INSTANCE = Mappers.getMapper(UserInfoMapping.class);

    @Mapping(target = "grantedIds", expression = "java(new java.util.ArrayList<>())")
    UserInfoDTO toDto(UserInfoEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserInfoEntity dto2entity(UserInfoDTO dto);
}
