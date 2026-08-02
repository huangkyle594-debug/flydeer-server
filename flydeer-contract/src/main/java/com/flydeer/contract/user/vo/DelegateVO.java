package com.flydeer.contract.user.vo;

import java.time.Instant;

public record DelegateVO(
    Long delegatorId,
    Long delegatedId,
    String status,
    Instant updatedAt) {
}
