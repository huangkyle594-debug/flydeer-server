package com.flydeer.structmind.service.user;

import com.flydeer.structmind.common.exception.ErrorCodes;
import com.flydeer.structmind.common.exception.business.BusinessException;
import com.flydeer.structmind.contract.user.enums.LoginChannel;
import com.flydeer.structmind.repository.entity.UserEntity;
import com.flydeer.structmind.repository.mapper.UserDelegateMapper;
import com.flydeer.structmind.repository.mapper.UserMapper;
import com.flydeer.structmind.service.user.utils.IdGenerateUtils;
import com.flydeer.structmind.service.user.model.OauthUserRecord;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserDelegateMapper userDelegateMapper;
    private final IdGenerateUtils idGenerateUtils;

    public UserService(
            UserMapper userMapper, UserDelegateMapper userDelegateMapper, IdGenerateUtils idGenerateUtils) {
        this.userMapper = userMapper;
        this.userDelegateMapper = userDelegateMapper;
        this.idGenerateUtils = idGenerateUtils;
    }

    public UserEntity requireActive(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodes.UNAUTHORIZED, "user not found");
        }
        if (user.getStatus() == null || user.getStatus() != UserEntity.STATUS_ACTIVE) {
            throw new BusinessException(ErrorCodes.FORBIDDEN, "user disabled");
        }
        return user;
    }

    public List<Long> listDelegatedUserIds(Long userId) {
        return userDelegateMapper.selectAcceptedGrantorIds(userId);
    }

    @Transactional
    public UserEntity loginOrRegisterPhone(String phone) {
        UserEntity existing = userMapper.selectByChannelAndUid(LoginChannel.PHONE.name(), phone);
        if (existing != null) {
            ensureActive(existing);
            return existing;
        }
        UserEntity byPhone = userMapper.selectByPhone(phone);
        if (byPhone != null) {
            ensureActive(byPhone);
            return byPhone;
        }
        UserEntity user = new UserEntity();
        user.setId(idGenerateUtils.nextUserId());
        user.setChannel(LoginChannel.PHONE.name());
        user.setChannelUid(phone);
        user.setPhone(phone);
        user.setVerified(1);
        user.setNickname(maskPhone(phone));
        user.setStatus(UserEntity.STATUS_ACTIVE);
        userMapper.insert(user);
        return user;
    }

    @Transactional
    public UserEntity loginOrRegisterOauth(LoginChannel channel, OauthUserRecord info) {
        UserEntity existing =
                userMapper.selectByChannelAndUid(channel.name(), info.channelUid());
        if (existing != null) {
            ensureActive(existing);
            return existing;
        }
        UserEntity user = new UserEntity();
        user.setId(idGenerateUtils.nextUserId());
        user.setChannel(channel.name());
        user.setChannelUid(info.channelUid());
        user.setPhone(null);
        user.setVerified(0);
        user.setNickname(trimNickname(info.username()));
        user.setStatus(UserEntity.STATUS_ACTIVE);
        userMapper.insert(user);
        return user;
    }

    @Transactional
    public UserEntity updateNickname(Long userId, String nickName) {
        UserEntity user = requireActive(userId);
        userMapper.updateNickname(userId, trimNickname(nickName));
        user.setNickname(trimNickname(nickName));
        return user;
    }

    @Transactional
    public UserEntity bindPhone(Long userId, String phone) {
        UserEntity user = requireActive(userId);
        if (user.getVerified() != null && user.getVerified() == 1 && phone.equals(user.getPhone())) {
            return user;
        }
        UserEntity occupied = userMapper.selectByPhone(phone);
        if (occupied != null && !occupied.getId().equals(userId)) {
            throw new BusinessException(ErrorCodes.CONFLICT, "phone already bound");
        }
        userMapper.bindPhone(userId, phone);
        user.setPhone(phone);
        user.setVerified(1);
        return user;
    }

    private void ensureActive(UserEntity user) {
        if (user.getStatus() == null || user.getStatus() != UserEntity.STATUS_ACTIVE) {
            throw new BusinessException(ErrorCodes.FORBIDDEN, "user disabled");
        }
    }

    private static String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return "user";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static String trimNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            return "user";
        }
        String trimmed = nickname.trim();
        return trimmed.length() > 64 ? trimmed.substring(0, 64) : trimmed;
    }
}
