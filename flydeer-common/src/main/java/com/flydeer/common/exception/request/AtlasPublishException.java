package com.flydeer.common.exception.request;

import com.flydeer.common.exception.ErrorCodes;

public class AtlasPublishException extends BadRequestException {

    public AtlasPublishException() {
        super(ErrorCodes.ATLAS_PUBLISH, "仅草稿状态的图集才可提交审核");
    }
}
