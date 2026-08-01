package com.flydeer.structmind.api.user;

import com.flydeer.structmind.common.exception.business.DelegateNotFoundException;
import com.flydeer.structmind.common.exception.business.UserInvalidException;
import com.flydeer.structmind.common.exception.business.UserNotFoundException;
import com.flydeer.structmind.common.exception.request.DelegateSelfException;
import com.flydeer.structmind.contract.user.UserDelegateApi;
import com.flydeer.structmind.contract.user.request.DelegateOperateRequest;
import com.flydeer.structmind.contract.user.request.QueryDelegateRequest;
import com.flydeer.structmind.api.user.mapper.UserDelegateMapper;
import com.flydeer.structmind.contract.user.vo.DelegateVO;
import com.flydeer.structmind.repository.mysql.dto.UserDelegateDTO;
import com.flydeer.structmind.service.user.UserDelegateService;
import com.flydeer.structmind.service.user.UserService;
import java.util.List;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDelegateApiImpl implements UserDelegateApi {

    private final UserDelegateService userDelegateService;

    private final UserService userService;

    @Override
    public void delegate(@Valid DelegateOperateRequest request)
        throws UserNotFoundException, UserInvalidException, DelegateSelfException {
        userService.requireActive(request.getOperateId());
        userDelegateService.delegate(request.getUserId(), request.getOperateId());
    }

    @Override
    public void accept(@Valid DelegateOperateRequest request)
        throws UserNotFoundException, UserInvalidException, DelegateNotFoundException {
        userService.requireActive(request.getUserId());
        userDelegateService.accept(request.getUserId(), request.getOperateId());
    }

    @Override
    public void revoke(@Valid DelegateOperateRequest request) throws DelegateNotFoundException {

        switch (request.getRelation()) {
            case MANAGING:
                userDelegateService.revoke(request.getUserId(), request.getOperateId());
                break;
            case MANAGED:
                userDelegateService.revoke(request.getOperateId(), request.getUserId());
                break;
            default:
        }
    }

    @Override
    public List<DelegateVO> queryDelegateRelation(QueryDelegateRequest request) {
        List<UserDelegateDTO> rows = userDelegateService.queryDelegations(
                request.getUserId(), request.statusNullIfEmpty(), request.getRelation());
        return UserDelegateMapper.INSTANCE.toVOList(rows);
    }
}
