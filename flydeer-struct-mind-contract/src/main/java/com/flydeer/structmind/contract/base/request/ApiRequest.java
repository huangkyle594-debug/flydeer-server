package com.flydeer.structmind.contract.base.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Base request filled by auth aspect.
 */
@Getter
@Setter
public class ApiRequest {

    private Long userId;

    private boolean verified;

    private List<Long> allUserIds;
}
