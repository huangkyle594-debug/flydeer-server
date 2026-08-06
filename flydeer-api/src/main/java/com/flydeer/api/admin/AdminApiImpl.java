package com.flydeer.api.admin;

import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.contract.admin.AdminApi;
import com.flydeer.contract.admin.request.DisableUserRequest;
import com.flydeer.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdminApiImpl implements AdminApi {

    private final UserService userService;

    @Override
    public void disableUser(@Valid DisableUserRequest request) throws UserNotFoundException {
        userService.disableUser(request.getOperatorId());
    }
}
