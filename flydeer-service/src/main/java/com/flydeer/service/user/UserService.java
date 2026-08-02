package com.flydeer.service.user;

import com.flydeer.common.constants.UserConstants;
import com.flydeer.common.exception.business.BindPhoneExceedException;
import com.flydeer.common.exception.business.PhoneChannelOperateException;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.utils.TextUtils;
import com.flydeer.contract.user.enums.LoginChannelEnum;
import com.flydeer.contract.user.enums.UserStatusEnum;
import com.flydeer.contract.user.enums.UserVerifiedStatusEnum;
import com.flydeer.repository.mysql.dto.UserDelegateDTO;
import com.flydeer.repository.mysql.dto.UserInfoDTO;
import com.flydeer.repository.mysql.option.user.UserOptions;
import com.flydeer.repository.mysql.repository.UserDelegateRepository;
import com.flydeer.repository.mysql.repository.UserInfoRepository;
import com.flydeer.service.user.model.OauthUserRecord;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserInfoRepository userInfoRepository;

    private final UserDelegateRepository userDelegateRepository;

    public UserInfoDTO requireActive(Long userId) throws UserNotFoundException, UserInvalidException {
        return requireActive(userId, UserOptions.option());
    }

    public UserInfoDTO requireActive(Long userId, UserOptions options) throws UserNotFoundException, UserInvalidException {
        UserInfoDTO user = userInfoRepository.queryById(userId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        if (!UserStatusEnum.STATUS_ACTIVE.getCode().equals(user.getStatus())) {
            throw new UserInvalidException();
        }
        if (options.hasDelegated()) {
            List<Long> delegators = userDelegateRepository.queryDelegate(userId, null, UserOptions.option())
                .stream().map(UserDelegateDTO::getDelegatedId).toList();
            user.setDelegatorIds(delegators);
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
        if (!UserStatusEnum.STATUS_ACTIVE.getCode().equals(user.getStatus())) {
            throw new UserInvalidException();
        }
    }

    public void ensureNotPhoneChannel(String channel) throws PhoneChannelOperateException {
        if (LoginChannelEnum.PHONE.name().equals(channel)) {
            throw new PhoneChannelOperateException();
        }
    }

}
