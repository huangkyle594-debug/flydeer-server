package com.flydeer.service.user.model;

import com.flydeer.contract.user.enums.UserStatusEnum;

public record AccessTokenClaims(long userId, boolean verified, int status) {

    public boolean active() {
        return UserStatusEnum.STATUS_ACTIVE.getCode().equals(status);
    }
}
