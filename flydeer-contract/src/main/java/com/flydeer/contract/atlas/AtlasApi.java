package com.flydeer.contract.atlas;

import com.flydeer.common.exception.auth.NeedLoginException;
import com.flydeer.common.exception.business.*;
import com.flydeer.common.exception.request.AtlasPublishException;
import com.flydeer.contract.atlas.request.AtlasCreateRequest;
import com.flydeer.contract.atlas.request.AtlasIdRequest;
import com.flydeer.contract.atlas.request.AtlasQuery;
import com.flydeer.contract.atlas.request.AtlasUpdateRequest;
import com.flydeer.contract.atlas.vo.AtlasVO;
import com.flydeer.contract.common.request.PageRequest;
import com.flydeer.contract.common.vo.PageVO;

import java.util.List;

public interface AtlasApi {

    PageVO<AtlasVO> pageQuery(PageRequest<AtlasQuery> request) throws NeedLoginException;

    List<String> listTags();

    AtlasVO createAtlas(AtlasCreateRequest request)
        throws UserNotFoundException, UserInvalidException;

    AtlasVO updateAtlas(AtlasUpdateRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException;

    void submitReview(AtlasIdRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, AtlasPublishException;

    void deleteAtlas(AtlasIdRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException;
}
