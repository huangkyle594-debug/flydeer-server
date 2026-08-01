package com.flydeer.contract.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    STATUS_ACTIVE(1),
    STATUS_DISABLED(0),
    ;

    private final Integer code;
}
