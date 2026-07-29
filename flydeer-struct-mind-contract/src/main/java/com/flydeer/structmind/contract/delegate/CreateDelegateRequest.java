package com.flydeer.structmind.contract.delegate;

import com.flydeer.structmind.contract.auth.BaseRequest;
import com.flydeer.structmind.contract.enums.DelegateRequestType;
import jakarta.validation.constraints.NotNull;

public class CreateDelegateRequest extends BaseRequest {

    @NotNull
    private Long peerUserId;

    @NotNull
    private DelegateRequestType requestType;

    public Long getPeerUserId() {
        return peerUserId;
    }

    public void setPeerUserId(Long peerUserId) {
        this.peerUserId = peerUserId;
    }

    public DelegateRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(DelegateRequestType requestType) {
        this.requestType = requestType;
    }
}
