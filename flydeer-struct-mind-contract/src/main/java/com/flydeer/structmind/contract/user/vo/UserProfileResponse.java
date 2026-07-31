package com.flydeer.structmind.contract.user.vo;

import java.util.List;

public record UserProfileResponse(
        Long userId,
        String channel,
        String nickName,
        boolean verified,
        String phone,
        List<Long> delegatedUserIds) {}
