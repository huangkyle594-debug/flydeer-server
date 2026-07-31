package com.flydeer.structmind.contract.base.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Base request filled by auth aspect.
 */
@Getter
public class ApiRequest {

    @Setter
    private Long userId;
    @Setter
    private String channel;
    @Setter
    private boolean verified;
    private List<Long> delegatedUserIds = new ArrayList<>();

    public void setDelegatedUserIds(List<Long> delegatedUserIds) {
        this.delegatedUserIds = delegatedUserIds != null ? delegatedUserIds : new ArrayList<>();
    }
}
