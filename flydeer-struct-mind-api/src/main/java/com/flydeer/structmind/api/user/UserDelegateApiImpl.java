package com.flydeer.structmind.api.user;

import com.flydeer.structmind.contract.user.UserDelegateApi;
import com.flydeer.structmind.contract.user.vo.DelegateItemResponse;
import com.flydeer.structmind.contract.user.enums.DelegateRequestType;
import com.flydeer.structmind.service.user.UserDelegateService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserDelegateApiImpl implements UserDelegateApi {

    private final UserDelegateService userDelegateService;

    public UserDelegateApiImpl(UserDelegateService userDelegateService) {
        this.userDelegateService = userDelegateService;
    }

    public void create(Long userId, Long peerUserId, DelegateRequestType requestType) {
        userDelegateService.delegate(userId, peerUserId, requestType);
    }

    public void accept(Long userId, Long peerUserId) {
        userDelegateService.accept(userId, peerUserId);
    }

    public void reject(Long userId, Long peerUserId) {
        userDelegateService.revoke(userId, peerUserId);
    }

    public void cancel(Long userId, Long peerUserId) {
        userDelegateService.cancel(userId, peerUserId);
    }

    public List<DelegateItemResponse> list(Long userId) {
        return userDelegateService.list(userId);
    }
}
