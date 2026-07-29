package com.flydeer.structmind.contract.auth;

import java.util.ArrayList;
import java.util.List;

/**
 * Base request filled by auth aspect.
 */
public class BaseRequest {

    private Long userId;
    private String channel;
    private boolean verified;
    private List<Long> delegatedUserIds = new ArrayList<>();

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public List<Long> getDelegatedUserIds() {
        return delegatedUserIds;
    }

    public void setDelegatedUserIds(List<Long> delegatedUserIds) {
        this.delegatedUserIds = delegatedUserIds != null ? delegatedUserIds : new ArrayList<>();
    }
}
