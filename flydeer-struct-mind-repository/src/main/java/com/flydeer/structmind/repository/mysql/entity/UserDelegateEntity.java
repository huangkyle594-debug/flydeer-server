package com.flydeer.structmind.repository.mysql.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
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
