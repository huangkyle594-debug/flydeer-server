package com.flydeer.structmind.api.delegate;

import com.flydeer.structmind.contract.delegate.DelegateItemResponse;
import com.flydeer.structmind.contract.enums.DelegateRequestType;
import com.flydeer.structmind.service.delegate.DelegateService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DelegateFacade {

    private final DelegateService delegateService;

    public DelegateFacade(DelegateService delegateService) {
        this.delegateService = delegateService;
    }

    public void create(Long userId, Long peerUserId, DelegateRequestType requestType) {
        delegateService.create(userId, peerUserId, requestType);
    }

    public void accept(Long userId, Long peerUserId) {
        delegateService.accept(userId, peerUserId);
    }

    public void reject(Long userId, Long peerUserId) {
        delegateService.reject(userId, peerUserId);
    }

    public void cancel(Long userId, Long peerUserId) {
        delegateService.cancel(userId, peerUserId);
    }

    public List<DelegateItemResponse> list(Long userId) {
        return delegateService.list(userId);
    }
}
