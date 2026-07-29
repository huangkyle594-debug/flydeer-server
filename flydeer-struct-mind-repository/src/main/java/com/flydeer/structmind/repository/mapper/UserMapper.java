package com.flydeer.structmind.repository.mapper;

import com.flydeer.structmind.repository.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    UserEntity selectById(@Param("id") Long id);

    UserEntity selectByChannelAndUid(@Param("channel") String channel, @Param("channelUid") String channelUid);

    UserEntity selectByPhone(@Param("phone") String phone);

    Long selectMaxId();

    int insert(UserEntity user);

    int updateNickname(@Param("id") Long id, @Param("nickname") String nickname);

    int bindPhone(@Param("id") Long id, @Param("phone") String phone);
}
