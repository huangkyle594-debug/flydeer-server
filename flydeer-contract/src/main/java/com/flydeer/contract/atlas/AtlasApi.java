package com.flydeer.contract.atlas;

import com.flydeer.common.exception.auth.NeedLoginException;
import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.exception.request.BadRequestException;
import com.flydeer.contract.atlas.request.AtlasCreateRequest;
import com.flydeer.contract.atlas.request.AtlasIdRequest;
import com.flydeer.contract.atlas.request.AtlasImportRequest;
import com.flydeer.contract.atlas.request.AtlasQueryRequest;
import com.flydeer.contract.atlas.request.AtlasUpdateRequest;
import com.flydeer.contract.atlas.vo.AtlasPageVO;
import com.flydeer.contract.atlas.vo.AtlasVO;

import java.util.List;

public interface AtlasApi {

    AtlasPageVO listAtlases(AtlasQueryRequest request) throws NeedLoginException;

    List<String> listTags();

    AtlasVO createAtlas(AtlasCreateRequest request)
        throws UserNotFoundException, UserInvalidException, BadRequestException;

    AtlasVO updateAtlas(AtlasUpdateRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, BadRequestException;

    void submitReview(AtlasIdRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, BadRequestException;

    void deleteAtlas(AtlasIdRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException;

    AtlasVO importAtlas(AtlasImportRequest request)
        throws UserNotFoundException, UserInvalidException, BadRequestException;

    AtlasVO getAtlas(AtlasIdRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException;
}
