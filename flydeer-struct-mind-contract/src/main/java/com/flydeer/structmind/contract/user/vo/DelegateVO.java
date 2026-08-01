package com.flydeer.structmind.contract.user.vo;

import java.time.Instant;

public record DelegateVO(
    Long userId,
    Long grantedUserId,
    String status,
    Instant updatedAt) {
}
