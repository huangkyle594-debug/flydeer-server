package com.flydeer.service.user;

import com.flydeer.common.constants.UserConstants;
import com.flydeer.common.exception.auth.NeedVerifyException;
import com.flydeer.common.exception.business.BindPhoneExceedException;
import com.flydeer.common.exception.business.PhoneChannelOperateException;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.utils.PhoneNumberUtils;
import com.flydeer.common.utils.TextUtils;
import com.flydeer.contract.user.enums.LoginChannelEnum;
import com.flydeer.contract.user.enums.UserStatusEnum;
import com.flydeer.contract.user.enums.UserVerifiedStatusEnum;
import com.flydeer.repository.mysql.dto.UserDelegateDTO;
import com.flydeer.repository.mysql.dto.UserInfoDTO;
import com.flydeer.repository.mysql.option.user.UserOptions;
import com.flydeer.repository.mysql.repository.UserDelegateRepository;
import com.flydeer.repository.mysql.repository.UserInfoRepository;
import com.flydeer.service.user.config.UserConfig;
import com.flydeer.service.user.event.UserDisabledEvent;
import com.flydeer.service.user.model.OauthUserRecord;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserInfoRepository userInfoRepository;

    private final UserDelegateRepository userDelegateRepository;

    private final UserConfig userConfig;

    private final ApplicationEventPublisher eventPublisher;

    public UserInfoDTO queryUser(Long userId)
        throws UserNotFoundException, UserInvalidException, NeedVerifyException {
        return queryUser(userId, UserOptions.option());
    }

    public UserInfoDTO queryUser(Long userId, UserOptions options)
        throws UserInvalidException, UserNotFoundException, NeedVerifyException {
        UserInfoDTO user = userInfoRepository.queryById(userId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        if (options.hasRequireActive()) {
            ensureActive(user);
        }
        if (options.hasRequireVerify()) {
            ensureVerify(user);
        }
        if (options.hasDelegated()) {
            List<Long> delegators = userDelegateRepository.queryDelegate(userId, null, UserOptions.option())
                .stream().map(UserDelegateDTO::getDelegatedId).toList();
            user.setDelegatorIds(delegators);
        }
        return user;
    }

    public UserInfoDTO loginOrRegisterPhone(String phone) throws UserInvalidException {
        String phoneHash = PhoneNumberUtils.hashPhone(phone, userConfig.getPhoneHashSalt());
        String maskedPhone = PhoneNumberUtils.maskPhone(phone);
        UserInfoDTO exist = userInfoRepository.selectByChannelAndUid(LoginChannelEnum.PHONE, phoneHash);
        if (exist != null) {
            ensureActive(exist);
            return exist;
        }
        return userInfoRepository.register(
            LoginChannelEnum.PHONE, phoneHash, maskedPhone, maskedPhone, phoneHash,
            UserOptions.option().loginUsePhone());
    }

    public UserInfoDTO loginOrRegisterOauth(LoginChannelEnum channel, OauthUserRecord info) throws UserInvalidException {
        UserInfoDTO exist = userInfoRepository.selectByChannelAndUid(channel, info.channelUid());
        if (exist != null) {
            ensureActive(exist);
            return exist;
        }
        return userInfoRepository.register(
            channel, info.channelUid(), info.username(), null, null, UserOptions.option());
    }

    public void updateUserName(Long userId, String userName) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(userId);
        dto.setName(TextUtils.trimText(userName, UserConstants.MAX_USER_NAME_LENGTH));
        userInfoRepository.update(dto);
    }

    public UserInfoDTO bindPhone(Long userId, String phone)
        throws UserNotFoundException, PhoneChannelOperateException, BindPhoneExceedException,
        UserInvalidException, NeedVerifyException {
        UserInfoDTO user = queryUser(userId);
        ensureNotPhoneChannel(user.getChannel());
        String phoneHash = PhoneNumberUtils.hashPhone(phone, userConfig.getPhoneHashSalt());
        String maskedPhone = PhoneNumberUtils.maskPhone(phone);
        List<UserInfoDTO> exists = userInfoRepository.selectByPhoneHash(phoneHash);
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
        dto.setPhone(maskedPhone);
        dto.setPhoneHash(phoneHash);
        userInfoRepository.update(dto);
        user.setVerified(UserVerifiedStatusEnum.VERIFIED.getCode());
        user.setPhone(maskedPhone);
        user.setPhoneHash(phoneHash);
        return user;
    }

    /**
     * Admin disable: set status=DISABLED and publish {@link UserDisabledEvent}.
     * Side effects (e.g. revoke delegates) are handled by async listeners.
     * Access tokens remain usable until expiry; refresh/login will fail.
     */
    @Transactional
    public void disableUser(Long userId) throws UserNotFoundException {
        UserInfoDTO user = userInfoRepository.queryById(userId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        if (UserStatusEnum.STATUS_DISABLED.getCode().equals(user.getStatus())) {
            return;
        }
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(userId);
        dto.setStatus(UserStatusEnum.STATUS_DISABLED.getCode());
        userInfoRepository.update(dto);
        eventPublisher.publishEvent(new UserDisabledEvent(userId));
    }

    private void ensureActive(UserInfoDTO user) throws UserInvalidException {
        if (!UserStatusEnum.STATUS_ACTIVE.getCode().equals(user.getStatus())) {
            throw new UserInvalidException();
        }
    }

    private void ensureVerify(UserInfoDTO user) throws NeedVerifyException {
        if (!UserVerifiedStatusEnum.VERIFIED.getCode().equals(user.getVerified())) {
            throw new NeedVerifyException();
        }
    }

    public void ensureNotPhoneChannel(String channel) throws PhoneChannelOperateException {
        if (LoginChannelEnum.PHONE.name().equals(channel)) {
            throw new PhoneChannelOperateException();
        }
    }

}
