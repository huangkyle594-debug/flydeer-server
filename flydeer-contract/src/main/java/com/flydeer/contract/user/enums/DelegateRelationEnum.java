package com.flydeer.contract.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DelegateRelationEnum {

    MANAGING("我代理的"),
    MANAGED("代理我的"),
    ;

    private final String desc;
}
