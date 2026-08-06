package com.flydeer.contract.atlas.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AtlasVisibleEnum {

    VISIBLE(1),
    UN_VISIBLE(0),
    ;

    private final Integer code;
}
