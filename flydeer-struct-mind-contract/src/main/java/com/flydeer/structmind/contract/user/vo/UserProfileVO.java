package com.flydeer.structmind.contract.user.vo;

public record UserProfileVO(
    Long userId,
    String channel,
    String name,
    Boolean verified,
    String phone) {
}
