package com.flydeer.structmind.contract.user.vo;

public record UserProfileVO(
    Long userId,
    String channel,
    String nickName,
    Boolean verified,
    String phone) {
}
