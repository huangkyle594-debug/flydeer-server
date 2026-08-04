package com.flydeer.repository.mysql.repository;

import com.flydeer.common.constants.UserConstants;
import com.flydeer.common.utils.TextUtils;
import com.flydeer.contract.user.enums.LoginChannelEnum;
import com.flydeer.contract.user.enums.UserStatusEnum;
import com.flydeer.contract.user.enums.UserVerifiedStatusEnum;
import com.flydeer.repository.mysql.dto.UserInfoDTO;
import com.flydeer.repository.mysql.entity.UserInfoEntity;
import com.flydeer.repository.mysql.entity.UserInfoEntityExample;
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

    private final IdGenerateUtils idGenerateUtils;

    public UserInfoDTO queryById(Long userId) {
        UserInfoEntityExample example = new UserInfoEntityExample();
        example.createCriteria().andIdEqualTo(userId);
        List<UserInfoEntity> rows = userInfoMapper.selectByExample(example);
        if (rows.isEmpty()) {
            return null;
        }
        return UserInfoMapping.INSTANCE.toDto(rows.getFirst());
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

    /**
     * @param channelUid  PHONE: phone hash; OAuth: platform uid
     * @param maskedPhone PHONE register: masked display phone; otherwise null
     * @param phoneHash   PHONE register / bind: SHA-256 hex; otherwise null
     */
    public UserInfoDTO register(LoginChannelEnum channel, String channelUid, String name,
                                String maskedPhone, String phoneHash, UserOptions options) {
        UserInfoEntity user = new UserInfoEntity();
        user.setId(idGenerateUtils.nextUserId());
        user.setChannel(channel.name());
        user.setChannelUid(channelUid);
        user.setStatus(UserStatusEnum.STATUS_ACTIVE.getCode());
        if (options.hasLoginUsePhone()) {
            user.setPhone(maskedPhone);
            user.setPhoneHash(phoneHash);
            user.setVerified(UserVerifiedStatusEnum.VERIFIED.getCode());
            user.setName(maskedPhone);
        } else {
            user.setPhone(null);
            user.setPhoneHash(null);
            user.setVerified(UserVerifiedStatusEnum.UN_VERIFIED.getCode());
            user.setName(TextUtils.trimText(name, UserConstants.MAX_USER_NAME_LENGTH));
        }
        userInfoMapper.insertSelective(user);
        return UserInfoMapping.INSTANCE.toDto(user);
    }

    public void update(UserInfoDTO dto) {
        UserInfoEntity entity = UserInfoMapping.INSTANCE.dto2entity(dto);
        userInfoMapper.updateByPrimaryKeySelective(entity);
    }

    public List<UserInfoDTO> selectByPhoneHash(String phoneHash) {
        UserInfoEntityExample example = new UserInfoEntityExample();
        example.createCriteria().andPhoneHashEqualTo(phoneHash);
        return userInfoMapper.selectByExample(example)
            .stream().map(UserInfoMapping.INSTANCE::toDto).toList();
    }
}
