package com.flydeer.repository.mysql.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDelegateEntity {
    private Long id;

    private Long delegatorId;

    private Long delegatedId;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}