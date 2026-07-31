package com.flydeer.structmind.repository.mysql.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
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

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_DISABLED = 0;
}
