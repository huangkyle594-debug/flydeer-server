package com.flydeer.repository.mysql.mapper;

import com.flydeer.repository.mysql.entity.UserDelegateEntity;
import com.flydeer.repository.mysql.entity.UserDelegateEntityExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserDelegateMapper {
    long countByExample(UserDelegateEntityExample example);

    int deleteByExample(UserDelegateEntityExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UserDelegateEntity row);

    int insertSelective(UserDelegateEntity row);

    List<UserDelegateEntity> selectByExample(UserDelegateEntityExample example);

    UserDelegateEntity selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") UserDelegateEntity row, @Param("example") UserDelegateEntityExample example);

    int updateByExample(@Param("row") UserDelegateEntity row, @Param("example") UserDelegateEntityExample example);

    int updateByPrimaryKeySelective(UserDelegateEntity row);

    int updateByPrimaryKey(UserDelegateEntity row);
}