package com.flydeer.api.user;

import com.flydeer.api.user.mapping.UserDelegateMapping;
import com.flydeer.common.exception.auth.NeedVerifyException;
import com.flydeer.common.exception.business.DelegateNotFoundException;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.exception.request.BadRequestException;
import com.flydeer.common.exception.request.DelegateSelfException;
import com.flydeer.contract.user.request.DelegateOperateRequest;
import com.flydeer.contract.user.request.QueryDelegateRequest;
import com.flydeer.contract.user.vo.DelegateVO;
import com.flydeer.repository.mysql.dto.UserDelegateDTO;
import com.flydeer.repository.mysql.option.user.UserOptions;
import com.flydeer.service.user.UserDelegateService;
import com.flydeer.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserDelegateApi implements com.flydeer.contract.user.UserDelegateApi {

    private final UserDelegateService userDelegateService;

    private final UserService userService;

    @Override
    public List<DelegateVO> queryDelegateRelation(@Valid QueryDelegateRequest request) {
        List<UserDelegateDTO> list = userDelegateService.queryDelegations(
            request.getUserId(), request.statusNullIfEmpty(), request.getRelation());
        return UserDelegateMapping.INSTANCE.toVOList(list);
    }

    @Override
    public void delegate(@Valid DelegateOperateRequest request)
        throws UserNotFoundException, UserInvalidException, DelegateSelfException, NeedVerifyException {
        userService.queryUser(request.getOperateId(), UserOptions.option().requireActive().requireVerify());
        userDelegateService.delegate(request.getUserId(), request.getOperateId());
    }

    @Override
    public void accept(@Valid DelegateOperateRequest request)
        throws UserNotFoundException, UserInvalidException, DelegateNotFoundException, NeedVerifyException {
        userService.queryUser(request.getOperateId(), UserOptions.option().requireActive().requireVerify());
        userDelegateService.accept(request.getOperateId(), request.getUserId());
    }

    @Override
    public void revoke(@Valid DelegateOperateRequest request) throws DelegateNotFoundException, BadRequestException {
        if (request.getRelation() == null) {
            throw new BadRequestException("身份不能为空");
        }
        switch (request.getRelation()) {
            case DELEGATOR:
                userDelegateService.revoke(request.getUserId(), request.getOperateId());
                break;
            case DELEGATED:
                userDelegateService.revoke(request.getOperateId(), request.getUserId());
                break;
            default:
        }
    }
}
