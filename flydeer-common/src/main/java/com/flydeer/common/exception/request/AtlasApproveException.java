package com.flydeer.common.exception.request;

import com.flydeer.common.exception.ErrorCodes;

public class AtlasApproveException extends BadRequestException {

    public AtlasApproveException() {
        super(ErrorCodes.ATLAS_APPROVE, "仅待审核状态的图集才可批准发布");
    }
}
