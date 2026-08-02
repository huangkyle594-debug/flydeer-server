package com.flydeer.common.exception.business;

import com.flydeer.common.exception.ErrorCodes;

public class AtlasForbiddenException extends BusinessException {

    public AtlasForbiddenException() {
        super(ErrorCodes.ATLAS_FORBIDDEN, "无权操作该图集");
    }
}
