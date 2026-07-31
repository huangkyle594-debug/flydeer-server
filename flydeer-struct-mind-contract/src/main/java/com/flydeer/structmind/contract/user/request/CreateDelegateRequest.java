package com.flydeer.structmind.contract.user.request;

import com.flydeer.structmind.contract.base.request.ApiRequest;
import com.flydeer.structmind.contract.user.enums.DelegateRequestType;
import jakarta.validation.constraints.NotNull;

public class CreateDelegateRequest extends ApiRequest {

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
