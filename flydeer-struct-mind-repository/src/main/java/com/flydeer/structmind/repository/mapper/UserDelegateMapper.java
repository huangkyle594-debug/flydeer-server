package com.flydeer.structmind.repository.mapper;

import com.flydeer.structmind.repository.entity.UserDelegateEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDelegateMapper {

    int insert(UserDelegateEntity entity);

    UserDelegateEntity selectActivePair(
            @Param("grantorId") Long grantorId, @Param("granteeId") Long granteeId);

    UserDelegateEntity selectPendingBetween(
            @Param("userId") Long userId, @Param("peerUserId") Long peerUserId);

    List<UserDelegateEntity> selectByUser(@Param("userId") Long userId);

    List<Long> selectAcceptedGrantorIds(@Param("granteeId") Long granteeId);

    int updateStatus(
            @Param("grantorId") Long grantorId,
            @Param("granteeId") Long granteeId,
            @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus);
}
