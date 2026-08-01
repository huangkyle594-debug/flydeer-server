package com.flydeer.repository.mysql.repository;

import com.flydeer.common.constants.UserConstants;
import com.flydeer.common.utils.PhoneNumberUtils;
import com.flydeer.common.utils.TextUtils;
import com.flydeer.contract.user.enums.DelegateStatusEnum;
import com.flydeer.contract.user.enums.LoginChannelEnum;
import com.flydeer.contract.user.enums.UserStatusEnum;
import com.flydeer.contract.user.enums.UserVerifiedStatusEnum;
import com.flydeer.repository.mysql.dto.UserInfoDTO;
import com.flydeer.repository.mysql.entity.UserDelegateEntity;
import com.flydeer.repository.mysql.entity.UserDelegateEntityExample;
import com.flydeer.repository.mysql.entity.UserInfoEntity;
import com.flydeer.repository.mysql.entity.UserInfoEntityExample;
import com.flydeer.repository.mysql.mapper.UserDelegateMapper;
import com.flydeer.repository.mysql.mapper.UserInfoMapper;
import com.flydeer.repository.mysql.mapping.UserInfoMapping;
import com.flydeer.repository.mysql.option.user.UserOptions;
import com.flydeer.repository.mysql.utils.IdGenerateUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class UserInfoRepository {

    private final UserInfoMapper userInfoMapper;

    private final UserDelegateMapper userDelegateMapper;

    private final IdGenerateUtils idGenerateUtils;

    public UserInfoDTO queryById(Long userId, UserOptions options) {
        UserInfoEntityExample example = new UserInfoEntityExample();
        example.createCriteria().andIdEqualTo(userId);
        List<UserInfoEntity> rows = userInfoMapper.selectByExample(example);
        if (rows.isEmpty()) {
            return null;
        }
        UserInfoDTO dto = UserInfoMapping.INSTANCE.toDto(rows.getFirst());
        if (options.hasWithGrantedUserIds()) {
            dto.getGrantedIds().addAll(queryGrantedIds(userId));
        }
        return dto;
    }

    private List<Long> queryGrantedIds(Long userId) {
        UserDelegateEntityExample delegateExample = new UserDelegateEntityExample();
        delegateExample.createCriteria()
            .andGrantedUserIdEqualTo(userId)
            .andStatusEqualTo(DelegateStatusEnum.ACCEPTED.name());
        List<UserDelegateEntity> delegates = userDelegateMapper.selectByExample(delegateExample);
        return delegates.stream().map(UserDelegateEntity::getUserId).toList();
    }

    public UserInfoDTO selectByChannelAndUid(LoginChannelEnum channel, String channelUid) {
        UserInfoEntityExample example = new UserInfoEntityExample();
        example.createCriteria()
            .andChannelEqualTo(channel.name())
            .andChannelUidEqualTo(channelUid);
        List<UserInfoEntity> rows = userInfoMapper.selectByExample(example);
        if (rows.isEmpty()) {
            return null;
        }
        return UserInfoMapping.INSTANCE.toDto(rows.getFirst());
    }

    public UserInfoDTO register(LoginChannelEnum channel, String channelUid, String name, UserOptions options) {
        UserInfoEntity user = new UserInfoEntity();
        user.setId(idGenerateUtils.nextUserId());
        user.setChannel(channel.name());
        user.setChannelUid(channelUid);
        user.setStatus(UserStatusEnum.STATUS_ACTIVE.getCode());
        if (options.hasLoginUsePhone()) {
            user.setPhone(channelUid);
            user.setVerified(UserVerifiedStatusEnum.VERIFIED.getCode());
            user.setName(PhoneNumberUtils.maskPhone(channelUid));
        } else {
            user.setPhone(null);
            user.setVerified(UserVerifiedStatusEnum.UN_VERIFIED.getCode());
            user.setName(TextUtils.trimText(name, UserConstants.MAX_USER_NAME_LENGTH));
        }
        userInfoMapper.insertSelective(user);
        return UserInfoMapping.INSTANCE.toDto(user);
    }

    public void update(UserInfoDTO dto, UserOptions options) {
        UserInfoEntity entity = UserInfoMapping.INSTANCE.dto2entity(dto);
        if (options.hasUpdateToNull()) {
            userInfoMapper.updateByPrimaryKey(entity);
        } else {
            userInfoMapper.updateByPrimaryKeySelective(entity);
        }
    }

    public List<UserInfoDTO> selectByPhone(String phone) {
        UserInfoEntityExample example = new UserInfoEntityExample();
        example.createCriteria().andPhoneEqualTo(phone);
        return userInfoMapper.selectByExample(example)
            .stream().map(UserInfoMapping.INSTANCE::toDto).toList();
    }
}
