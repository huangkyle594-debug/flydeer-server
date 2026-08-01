package com.flydeer.structmind.contract.user;

import com.flydeer.structmind.common.exception.business.DelegateNotFoundException;
import com.flydeer.structmind.common.exception.business.UserInvalidException;
import com.flydeer.structmind.common.exception.business.UserNotFoundException;
import com.flydeer.structmind.common.exception.request.DelegateSelfException;
import com.flydeer.structmind.contract.user.request.DelegateOperateRequest;
import com.flydeer.structmind.contract.user.request.QueryDelegateRequest;
import com.flydeer.structmind.contract.user.vo.DelegateVO;

import java.util.List;

public interface UserDelegateApi {

    void delegate(DelegateOperateRequest request)
        throws UserNotFoundException, UserInvalidException, DelegateSelfException;

    void accept(DelegateOperateRequest request)
        throws UserNotFoundException, UserInvalidException, DelegateNotFoundException;

    void revoke(DelegateOperateRequest request) throws DelegateNotFoundException;

    List<DelegateVO> queryDelegateRelation(QueryDelegateRequest request);
}
