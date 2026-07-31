package com.flydeer.structmind.repository.mysql.mapper;

import com.flydeer.structmind.repository.mysql.entity.UserDelegateEntity;

public interface UserDelegateMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserDelegateEntity row);

    int insertSelective(UserDelegateEntity row);

    UserDelegateEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserDelegateEntity row);

    int updateByPrimaryKey(UserDelegateEntity row);
}