package com.flydeer.structmind.service.user;

import com.flydeer.structmind.common.exception.ErrorCodes;
import com.flydeer.structmind.common.exception.business.BusinessException;
import com.flydeer.structmind.contract.user.vo.DelegateItemResponse;
import com.flydeer.structmind.contract.user.enums.DelegateRequestType;
import com.flydeer.structmind.contract.user.enums.DelegateStatus;
import com.flydeer.structmind.repository.mysql.entity.UserDelegateEntity;
import com.flydeer.structmind.repository.mysql.entity.UserInfoEntity;
import com.flydeer.structmind.repository.mysql.mapper.UserDelegateMapper;
import com.flydeer.structmind.repository.mysql.mapper.UserInfoMapper;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDelegateService {

    private final UserDelegateMapper delegateMapper;
    private final UserInfoMapper userInfoMapper;

    public UserDelegateService(UserDelegateMapper delegateMapper, UserInfoMapper userInfoMapper) {
        this.delegateMapper = delegateMapper;
        this.userInfoMapper = userInfoMapper;
    }

    @Transactional
    public void create(Long userId, Long peerUserId, DelegateRequestType requestType) {
        if (userId.equals(peerUserId)) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "cannot delegate to self");
        }
        requireUser(peerUserId);
        Long grantorId;
        Long granteeId;
        if (requestType == DelegateRequestType.GRANT) {
            grantorId = userId;
            granteeId = peerUserId;
        } else {
            grantorId = peerUserId;
            granteeId = userId;
        }
        UserDelegateEntity active = delegateMapper.selectActivePair(grantorId, granteeId);
        if (active != null) {
            throw new BusinessException(ErrorCodes.CONFLICT, "delegate already exists");
        }
        UserDelegateEntity entity = new UserDelegateEntity();
        entity.setGrantorId(grantorId);
        entity.setGranteeId(granteeId);
        entity.setRequestType(requestType.name());
        entity.setStatus(DelegateStatus.PENDING.name());
        delegateMapper.insert(entity);
    }

    @Transactional
    public void accept(Long userId, Long peerUserId) {
        respond(userId, peerUserId, DelegateStatus.ACCEPTED);
    }

    @Transactional
    public void reject(Long userId, Long peerUserId) {
        respond(userId, peerUserId, DelegateStatus.REJECTED);
    }

    @Transactional
    public void cancel(Long userId, Long peerUserId) {
        UserDelegateEntity pending = delegateMapper.selectPendingBetween(userId, peerUserId);
        if (pending != null) {
            int updated = delegateMapper.updateStatus(
                    pending.getGrantorId(),
                    pending.getGranteeId(),
                    DelegateStatus.PENDING.name(),
                    DelegateStatus.CANCELLED.name());
            if (updated > 0) {
                return;
            }
        }
        // cancel accepted where current user is grantor or grantee
        UserDelegateEntity asGrantor = delegateMapper.selectActivePair(userId, peerUserId);
        if (asGrantor != null && DelegateStatus.ACCEPTED.name().equals(asGrantor.getStatus())) {
            delegateMapper.updateStatus(
                    userId, peerUserId, DelegateStatus.ACCEPTED.name(), DelegateStatus.CANCELLED.name());
            return;
        }
        UserDelegateEntity asGrantee = delegateMapper.selectActivePair(peerUserId, userId);
        if (asGrantee != null && DelegateStatus.ACCEPTED.name().equals(asGrantee.getStatus())) {
            delegateMapper.updateStatus(
                    peerUserId, userId, DelegateStatus.ACCEPTED.name(), DelegateStatus.CANCELLED.name());
            return;
        }
        throw new BusinessException(ErrorCodes.NOT_FOUND, "delegate not found");
    }

    public List<DelegateItemResponse> list(Long userId) {
        return delegateMapper.selectByUser(userId).stream()
                .map(entity -> toResponse(userId, entity))
                .toList();
    }

    private void respond(Long userId, Long peerUserId, DelegateStatus target) {
        UserDelegateEntity pending = delegateMapper.selectPendingBetween(userId, peerUserId);
        if (pending == null) {
            throw new BusinessException(ErrorCodes.NOT_FOUND, "pending delegate not found");
        }
        // Only the counterparty who would be affected should accept/reject:
        // GRANT: grantor=requester, grantee=peer → peer accepts
        // RECEIVE: grantor=peer, grantee=requester → peer (grantor) accepts
        boolean canRespond = pending.getGrantorId().equals(userId) || pending.getGranteeId().equals(userId);
        if (!canRespond || userId.equals(resolveRequester(pending))) {
            // Allow either party that is not solely the initiator? Spec: accept authorization request.
            // The peer (non-initiator relative to request) should respond.
            Long initiator = resolveRequester(pending);
            if (userId.equals(initiator)) {
                throw new BusinessException(ErrorCodes.FORBIDDEN, "initiator cannot accept/reject");
            }
        }
        int updated = delegateMapper.updateStatus(
                pending.getGrantorId(),
                pending.getGranteeId(),
                DelegateStatus.PENDING.name(),
                target.name());
        if (updated == 0) {
            throw new BusinessException(ErrorCodes.CONFLICT, "delegate status changed");
        }
    }

    private Long resolveRequester(UserDelegateEntity pending) {
        if (DelegateRequestType.GRANT.name().equals(pending.getRequestType())) {
            return pending.getGrantorId();
        }
        return pending.getGranteeId();
    }

    private DelegateItemResponse toResponse(Long userId, UserDelegateEntity entity) {
        Long peer = entity.getGrantorId().equals(userId) ? entity.getGranteeId() : entity.getGrantorId();
        return new DelegateItemResponse(
                entity.getGrantorId(),
                entity.getGranteeId(),
                peer,
                DelegateRequestType.valueOf(entity.getRequestType()),
                DelegateStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt() == null
                        ? null
                        : entity.getCreatedAt().toInstant(ZoneOffset.UTC),
                entity.getRespondedAt() == null
                        ? null
                        : entity.getRespondedAt().toInstant(ZoneOffset.UTC));
    }

    private void requireUser(Long userId) {
        UserInfoEntity user = userInfoMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodes.NOT_FOUND, "peer user not found");
        }
    }
}
