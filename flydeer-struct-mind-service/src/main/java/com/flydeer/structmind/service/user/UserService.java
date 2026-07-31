package com.flydeer.structmind.service.user;

import com.flydeer.structmind.common.exception.ErrorCodes;
import com.flydeer.structmind.common.exception.business.BusinessException;
import com.flydeer.structmind.contract.user.enums.LoginChannel;
import com.flydeer.structmind.repository.mysql.entity.UserInfoEntity;
import com.flydeer.structmind.repository.mysql.mapper.UserDelegateMapper;
import com.flydeer.structmind.repository.mysql.mapper.UserInfoMapper;
import com.flydeer.structmind.service.user.utils.IdGenerateUtils;
import com.flydeer.structmind.service.user.model.OauthUserRecord;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final UserInfoMapper userInfoMapper;
    private final UserDelegateMapper userDelegateMapper;
    private final IdGenerateUtils idGenerateUtils;

    public UserService(
            UserInfoMapper userInfoMapper, UserDelegateMapper userDelegateMapper, IdGenerateUtils idGenerateUtils) {
        this.userInfoMapper = userInfoMapper;
        this.userDelegateMapper = userDelegateMapper;
        this.idGenerateUtils = idGenerateUtils;
    }

    public UserInfoEntity requireActive(Long userId) {
        UserInfoEntity user = userInfoMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodes.UNAUTHORIZED, "user not found");
        }
        if (user.getStatus() == null || user.getStatus() != UserInfoEntity.STATUS_ACTIVE) {
            throw new BusinessException(ErrorCodes.FORBIDDEN, "user disabled");
        }
        return user;
    }

    public List<Long> listDelegatedUserIds(Long userId) {
        return userDelegateMapper.selectAcceptedGrantorIds(userId);
    }

    @Transactional
    public UserInfoEntity loginOrRegisterPhone(String phone) {
        UserInfoEntity existing = userInfoMapper.selectByChannelAndUid(LoginChannel.PHONE.name(), phone);
        if (existing != null) {
            ensureActive(existing);
            return existing;
        }
        UserInfoEntity byPhone = userInfoMapper.selectByPhone(phone);
        if (byPhone != null) {
            ensureActive(byPhone);
            return byPhone;
        }
        UserInfoEntity user = new UserInfoEntity();
        user.setId(idGenerateUtils.nextUserId());
        user.setChannel(LoginChannel.PHONE.name());
        user.setChannelUid(phone);
        user.setPhone(phone);
        user.setVerified(1);
        user.setNickname(maskPhone(phone));
        user.setStatus(UserInfoEntity.STATUS_ACTIVE);
        userInfoMapper.insert(user);
        return user;
    }

    @Transactional
    public UserInfoEntity loginOrRegisterOauth(LoginChannel channel, OauthUserRecord info) {
        UserInfoEntity existing =
                userInfoMapper.selectByChannelAndUid(channel.name(), info.channelUid());
        if (existing != null) {
            ensureActive(existing);
            return existing;
        }
        UserInfoEntity user = new UserInfoEntity();
        user.setId(idGenerateUtils.nextUserId());
        user.setChannel(channel.name());
        user.setChannelUid(info.channelUid());
        user.setPhone(null);
        user.setVerified(0);
        user.setNickname(trimNickname(info.username()));
        user.setStatus(UserInfoEntity.STATUS_ACTIVE);
        userInfoMapper.insert(user);
        return user;
    }

    @Transactional
    public UserInfoEntity updateNickname(Long userId, String nickName) {
        UserInfoEntity user = requireActive(userId);
        userInfoMapper.updateNickname(userId, trimNickname(nickName));
        user.setNickname(trimNickname(nickName));
        return user;
    }

    @Transactional
    public UserInfoEntity bindPhone(Long userId, String phone) {
        UserInfoEntity user = requireActive(userId);
        if (user.getVerified() != null && user.getVerified() == 1 && phone.equals(user.getPhone())) {
            return user;
        }
        UserInfoEntity occupied = userInfoMapper.selectByPhone(phone);
        if (occupied != null && !occupied.getId().equals(userId)) {
            throw new BusinessException(ErrorCodes.CONFLICT, "phone already bound");
        }
        userInfoMapper.bindPhone(userId, phone);
        user.setPhone(phone);
        user.setVerified(1);
        return user;
    }

    private void ensureActive(UserInfoEntity user) {
        if (user.getStatus() == null || user.getStatus() != UserInfoEntity.STATUS_ACTIVE) {
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
