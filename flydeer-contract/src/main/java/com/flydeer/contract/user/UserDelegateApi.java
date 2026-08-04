package com.flydeer.contract.user;

import com.flydeer.common.exception.auth.NeedVerifyException;
import com.flydeer.common.exception.business.DelegateNotFoundException;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.exception.request.BadRequestException;
import com.flydeer.common.exception.request.DelegateSelfException;
import com.flydeer.contract.user.request.DelegateOperateRequest;
import com.flydeer.contract.user.request.QueryDelegateRequest;
import com.flydeer.contract.user.vo.DelegateVO;

import java.util.List;

public interface UserDelegateApi {

    List<DelegateVO> queryDelegateRelation(QueryDelegateRequest request);

    void delegate(DelegateOperateRequest request)
        throws UserNotFoundException, UserInvalidException, DelegateSelfException, NeedVerifyException;

    void accept(DelegateOperateRequest request)
        throws UserNotFoundException, UserInvalidException, DelegateNotFoundException, NeedVerifyException;

    void revoke(DelegateOperateRequest request) throws DelegateNotFoundException, BadRequestException;
}
