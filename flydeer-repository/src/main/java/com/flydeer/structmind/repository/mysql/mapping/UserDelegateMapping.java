package com.flydeer.structmind.repository.mysql.mapping;

import com.flydeer.structmind.repository.mysql.dto.UserDelegateDTO;
import com.flydeer.structmind.repository.mysql.entity.UserDelegateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserDelegateMapping {

    UserDelegateMapping INSTANCE = Mappers.getMapper(UserDelegateMapping.class);

    UserDelegateDTO toDto(UserDelegateEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserDelegateEntity dto2entity(UserDelegateDTO dto);
}
