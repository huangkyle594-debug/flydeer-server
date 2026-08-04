package com.flydeer.repository.mysql.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserInfoDTO {

    private Long id;

    private String channel;

    private String channelUid;

    private String phone;

    private String phoneHash;

    private Integer verified;

    private String name;

    private Integer status;

    private List<Long> delegatorIds;
}
