package com.flydeer.contract.admin;

import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.contract.user.request.DisableUserRequest;

public interface AdminApi {

    void disable(DisableUserRequest request) throws UserNotFoundException;

}
