package com.flydeer.structmind.service.user;

import com.flydeer.structmind.common.constants.UserConstants;
import com.flydeer.structmind.common.exception.business.BindPhoneExceedException;
import com.flydeer.structmind.common.exception.business.PhoneChannelOperateException;
import com.flydeer.structmind.common.exception.business.UserInvalidException;
import com.flydeer.structmind.common.exception.business.UserNotFoundException;
import com.flydeer.structmind.common.utils.TextUtils;
import com.flydeer.structmind.contract.user.enums.LoginChannelEnum;
import com.flydeer.structmind.contract.user.enums.UserStatusEnum;
import com.flydeer.structmind.contract.user.enums.UserVerifiedStatusEnum;
import com.flydeer.structmind.repository.mysql.dto.UserInfoDTO;
import com.flydeer.structmind.repository.mysql.option.user.UserOptions;
import com.flydeer.structmind.repository.mysql.repository.UserInfoRepository;
import com.flydeer.structmind.service.user.model.OauthUserRecord;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserInfoRepository userInfoRepository;

    public UserInfoDTO requireActive(Long userId) throws UserNotFoundException, UserInvalidException {
        UserInfoDTO user = userInfoRepository.queryById(userId, UserOptions.option());
        if (user == null) {
            throw new UserNotFoundException();
        }
        if (!UserStatusEnum.STATUS_ACTIVE.getCode().equals(user.getStatus())) {
            throw new UserInvalidException();
        }
        return user;
    }

    public UserInfoDTO loginOrRegisterPhone(String phone) throws UserInvalidException {
        UserInfoDTO exist = userInfoRepository.selectByChannelAndUid(LoginChannelEnum.PHONE, phone);
        if (exist != null) {
            ensureActive(exist);
            return exist;
        }
        return userInfoRepository.register(LoginChannelEnum.PHONE, phone, phone, UserOptions.option().loginUsePhone());
    }

    public UserInfoDTO loginOrRegisterOauth(LoginChannelEnum channel, OauthUserRecord info) throws UserInvalidException {
        UserInfoDTO exist = userInfoRepository.selectByChannelAndUid(channel, info.channelUid());
        if (exist != null) {
            ensureActive(exist);
            return exist;
        }
        return userInfoRepository.register(channel, info.channelUid(), info.username(), UserOptions.option());
    }

    public void updateUserName(Long userId, String userName) throws UserNotFoundException, UserInvalidException {
        requireActive(userId);
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(userId);
        dto.setName(TextUtils.trimText(userName, UserConstants.MAX_USER_NAME_LENGTH));
        userInfoRepository.update(dto, UserOptions.option());
    }

    public void bindPhone(Long userId, String phone)
        throws UserNotFoundException, UserInvalidException, PhoneChannelOperateException, BindPhoneExceedException {
        UserInfoDTO user = requireActive(userId);
        ensureNotPhoneChannel(user.getChannel());
        List<UserInfoDTO> exists = userInfoRepository.selectByPhone(phone);
        List<UserInfoDTO> bound = exists.stream()
            .filter(e -> user.getChannel().equals(e.getChannel()))
            .filter(e -> !e.getId().equals(userId))
            .toList();
        if (!bound.isEmpty()) {
            throw new BindPhoneExceedException();
        }

        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(userId);
        dto.setVerified(UserVerifiedStatusEnum.VERIFIED.getCode());
        dto.setPhone(phone);
        userInfoRepository.update(dto, UserOptions.option());
    }

    // todo
    //  封禁账号，同时revoke所有授权关系

    private void ensureActive(UserInfoDTO user) throws UserInvalidException {
        if (UserStatusEnum.STATUS_ACTIVE.getCode().equals(user.getStatus())) {
            throw new UserInvalidException();
        }
    }

    public void ensureNotPhoneChannel(String channel) throws PhoneChannelOperateException {
        if (LoginChannelEnum.PHONE.name().equals(channel)) {
            throw new PhoneChannelOperateException();
        }
    }

}
