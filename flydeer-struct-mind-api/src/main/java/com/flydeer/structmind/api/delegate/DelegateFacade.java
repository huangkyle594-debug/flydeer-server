package com.flydeer.structmind.api.delegate;

import com.flydeer.structmind.contract.delegate.DelegateItemResponse;
import com.flydeer.structmind.contract.enums.DelegateRequestType;
import com.flydeer.structmind.service.service.user.UserDelegateService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DelegateFacade {

    private final UserDelegateService userDelegateService;

    public DelegateFacade(UserDelegateService userDelegateService) {
        this.userDelegateService = userDelegateService;
    }

    public void create(Long userId, Long peerUserId, DelegateRequestType requestType) {
        userDelegateService.create(userId, peerUserId, requestType);
    }

    public void accept(Long userId, Long peerUserId) {
        userDelegateService.accept(userId, peerUserId);
    }

    public void reject(Long userId, Long peerUserId) {
        userDelegateService.reject(userId, peerUserId);
    }

    public void cancel(Long userId, Long peerUserId) {
        userDelegateService.cancel(userId, peerUserId);
    }

    public List<DelegateItemResponse> list(Long userId) {
        return userDelegateService.list(userId);
    }
}
