package com.flydeer.contract.atlas.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 图集列表权限范围筛选。
 */
public enum AtlasPermissionScope {
    ALL,
    CREATED,
    MANAGED;

    @JsonCreator
    public static AtlasPermissionScope from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        return AtlasPermissionScope.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
