package com.flydeer.structmind.api.user;

import com.flydeer.structmind.api.user.mapper.UserDelegateMapper;
import com.flydeer.structmind.common.exception.business.DelegateNotFoundException;
import com.flydeer.structmind.common.exception.business.UserInvalidException;
import com.flydeer.structmind.common.exception.business.UserNotFoundException;
import com.flydeer.structmind.common.exception.request.BadRequestException;
import com.flydeer.structmind.common.exception.request.DelegateSelfException;
import com.flydeer.structmind.contract.user.UserDelegateApi;
import com.flydeer.structmind.contract.user.request.DelegateOperateRequest;
import com.flydeer.structmind.contract.user.request.QueryDelegateRequest;
import com.flydeer.structmind.contract.user.vo.DelegateVO;
import com.flydeer.structmind.repository.mysql.dto.UserDelegateDTO;
import com.flydeer.structmind.service.user.UserDelegateService;
import com.flydeer.structmind.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserDelegateApiImpl implements UserDelegateApi {

    private final UserDelegateService userDelegateService;

    private final UserService userService;

    @Override
    public List<DelegateVO> queryDelegateRelation(@Valid QueryDelegateRequest request) {
        List<UserDelegateDTO> list = userDelegateService.queryDelegations(
            request.getUserId(), request.statusNullIfEmpty(), request.getRelation());
        return UserDelegateMapper.INSTANCE.toVOList(list);
    }

    @Override
    public void delegate(@Valid DelegateOperateRequest request)
        throws UserNotFoundException, UserInvalidException, DelegateSelfException {
        userService.requireActive(request.getOperateId());
        userDelegateService.delegate(request.getUserId(), request.getOperateId());
    }

    @Override
    public void accept(@Valid DelegateOperateRequest request)
        throws UserNotFoundException, UserInvalidException, DelegateNotFoundException {
        userService.requireActive(request.getOperateId());
        userDelegateService.accept(request.getOperateId(), request.getUserId());
    }

    @Override
    public void revoke(@Valid DelegateOperateRequest request) throws DelegateNotFoundException, BadRequestException {
        if (request.getRelation() == null) {
            throw new BadRequestException("身份不能为空");
        }
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
}
