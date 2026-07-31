package com.flydeer.structmind.repository.mysql.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDelegateEntity {
    private Long id;

    private Long grantorId;

    private Long granteeId;

    private String requestType;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime respondedAt;
}