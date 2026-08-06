package com.flydeer.contract.admin;

import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.business.AtlasNotVisibleException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.exception.request.AtlasApproveException;
import com.flydeer.contract.admin.request.DisableUserRequest;
import com.flydeer.contract.atlas.request.AtlasIdRequest;
import com.flydeer.contract.atlas.request.AtlasQuery;
import com.flydeer.contract.atlas.vo.AtlasVO;
import com.flydeer.contract.common.request.PageRequest;
import com.flydeer.contract.common.vo.PageVO;

public interface AdminApi {

    void disableUser(DisableUserRequest request) throws UserNotFoundException;

    /** Pending-review atlas queue (status=PENDING). */
    PageVO<AtlasVO> pagePendingAtlases(PageRequest<AtlasQuery> request);

    /** Approve publish: PENDING → PUBLISHED + visible. */
    void approveAtlas(AtlasIdRequest request)
        throws AtlasNotFoundException, AtlasApproveException, AtlasNotVisibleException, AtlasForbiddenException;
}
