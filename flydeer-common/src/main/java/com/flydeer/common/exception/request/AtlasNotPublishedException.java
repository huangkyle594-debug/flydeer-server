package com.flydeer.common.exception.request;

import com.flydeer.common.exception.ErrorCodes;

public class AtlasNotPublishedException extends BadRequestException {

    public AtlasNotPublishedException() {
        super(ErrorCodes.ATLAS_NOT_PUBLISHED, "不能查看未发布的图集");
    }
}
