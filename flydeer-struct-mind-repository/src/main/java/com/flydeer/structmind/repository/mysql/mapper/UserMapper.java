package com.flydeer.structmind.repository.mysql.mapper;

import com.flydeer.structmind.repository.mysql.entity.UserEntity;

public interface UserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserEntity row);

    int insertSelective(UserEntity row);

    UserEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserEntity row);

    int updateByPrimaryKey(UserEntity row);
}