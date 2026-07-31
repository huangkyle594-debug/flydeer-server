package com.flydeer.structmind.repository.mysql.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserEntity {
    private Long id;

    private String channel;

    private String channelUid;

    private String phone;

    private Integer verified;

    private String nickname;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}