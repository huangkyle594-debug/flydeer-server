package com.flydeer.structmind.contract.delegate;

import com.flydeer.structmind.contract.enums.DelegateRequestType;
import com.flydeer.structmind.contract.enums.DelegateStatus;
import java.time.Instant;

public record DelegateItemResponse(
        Long grantorId,
        Long granteeId,
        Long peerUserId,
        DelegateRequestType requestType,
        DelegateStatus status,
        Instant createdAt,
        Instant respondedAt) {}
