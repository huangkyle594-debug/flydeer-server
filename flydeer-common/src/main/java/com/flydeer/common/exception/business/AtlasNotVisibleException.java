package com.flydeer.common.exception.business;

import com.flydeer.common.exception.ErrorCodes;

public class AtlasNotVisibleException extends BusinessException {

    public AtlasNotVisibleException() {
        super(ErrorCodes.ATLAS_NOT_VISIBLE, "图集处于不可见状态");
    }
}
