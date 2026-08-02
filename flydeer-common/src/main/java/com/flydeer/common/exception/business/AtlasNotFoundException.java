package com.flydeer.common.exception.business;

import com.flydeer.common.exception.ErrorCodes;

public class AtlasNotFoundException extends BusinessException {

    public AtlasNotFoundException() {
        super(ErrorCodes.ATLAS_NOT_FOUND, "图集不存在");
    }
}
