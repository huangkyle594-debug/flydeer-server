package com.flydeer.structmind.contract.user.vo;

import com.flydeer.structmind.contract.user.enums.DelegateRequestType;
import com.flydeer.structmind.contract.user.enums.DelegateStatus;
import java.time.Instant;

public record DelegateItemResponse(
        Long grantorId,
        Long granteeId,
        Long peerUserId,
        DelegateRequestType requestType,
        DelegateStatus status,
        Instant createdAt,
        Instant respondedAt) {}
