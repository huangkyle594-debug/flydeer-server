package com.flydeer.structmind.contract.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserVerifiedStatusEnum {

    VERIFIED(1),
    UN_VERIFIED(0),
    ;

    private final int code;
}
