package com.flydeer.contract.admin;

import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.contract.admin.request.DisableUserRequest;

public interface AdminApi {

    void disableUser(DisableUserRequest request) throws UserNotFoundException;
}
