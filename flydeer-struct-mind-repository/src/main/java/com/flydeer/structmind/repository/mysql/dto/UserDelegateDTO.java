package com.flydeer.structmind.repository.mysql.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDelegateDTO {

    private Long id;

    private Long userId;

    private Long grantedUserId;

    private String status;
}
