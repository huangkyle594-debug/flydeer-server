package com.flydeer.structmind.repository.mysql.mapper;

import com.flydeer.structmind.repository.mysql.entity.UserInfoEntity;

public interface UserInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserInfoEntity row);

    int insertSelective(UserInfoEntity row);

    UserInfoEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserInfoEntity row);

    int updateByPrimaryKey(UserInfoEntity row);
}