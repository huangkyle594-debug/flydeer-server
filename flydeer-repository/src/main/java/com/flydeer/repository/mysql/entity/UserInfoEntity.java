package com.flydeer.repository.mysql.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserInfoEntity {

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_DISABLED = 0;

    private Long id;

    private String channel;

    private String channelUid;

    private String phone;

    private Integer verified;

    private String name;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}