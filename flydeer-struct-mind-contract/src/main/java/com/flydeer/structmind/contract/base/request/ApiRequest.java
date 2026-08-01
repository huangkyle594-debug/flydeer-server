package com.flydeer.structmind.contract.base.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Base request filled by auth aspect.
 */
@Getter
@Setter
@NoArgsConstructor
public class ApiRequest {

    private Long userId;

    private boolean verified;

    private List<Long> allUserIds;

    public ApiRequest(ApiRequest auth) {
        if (auth == null) {
            return;
        }
        this.userId = auth.userId;
        this.verified = auth.verified;
        this.allUserIds = auth.allUserIds;
    }
}
