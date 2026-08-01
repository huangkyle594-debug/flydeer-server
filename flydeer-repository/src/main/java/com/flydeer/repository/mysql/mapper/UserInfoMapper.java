package com.flydeer.repository.mysql.mapper;

import com.flydeer.repository.mysql.entity.UserInfoEntity;
import com.flydeer.repository.mysql.entity.UserInfoEntityExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserInfoMapper {
    long countByExample(UserInfoEntityExample example);

    int deleteByExample(UserInfoEntityExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UserInfoEntity row);

    int insertSelective(UserInfoEntity row);

    List<UserInfoEntity> selectByExample(UserInfoEntityExample example);

    UserInfoEntity selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") UserInfoEntity row, @Param("example") UserInfoEntityExample example);

    int updateByExample(@Param("row") UserInfoEntity row, @Param("example") UserInfoEntityExample example);

    int updateByPrimaryKeySelective(UserInfoEntity row);

    int updateByPrimaryKey(UserInfoEntity row);
}