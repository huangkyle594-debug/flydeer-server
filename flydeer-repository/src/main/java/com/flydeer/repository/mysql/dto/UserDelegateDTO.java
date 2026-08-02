package com.flydeer.repository.mysql.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDelegateDTO {

    private Long id;

    private Long delegatorId;

    private Long delegatedId;

    private String status;

    private LocalDateTime updatedAt;
}
