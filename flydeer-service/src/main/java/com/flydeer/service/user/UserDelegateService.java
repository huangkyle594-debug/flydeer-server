package com.flydeer.service.user;

import com.flydeer.common.exception.business.DelegateNotFoundException;
import com.flydeer.common.exception.request.DelegateSelfException;
import com.flydeer.contract.user.enums.DelegateRelationEnum;
import com.flydeer.contract.user.enums.DelegateStatusEnum;
import com.flydeer.repository.mysql.dto.UserDelegateDTO;
import com.flydeer.repository.mysql.option.user.UserOptions;
import com.flydeer.repository.mysql.repository.UserDelegateRepository;
import com.flydeer.service.user.event.UserDisabledEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserDelegateService {

    private final UserDelegateRepository userDelegateRepository;

    public List<UserDelegateDTO> queryDelegations(
        Long userId, List<String> status, DelegateRelationEnum relation) {
        UserOptions options = UserOptions.option();
        if (DelegateRelationEnum.DELEGATED.equals(relation)) {
            options.delegated();
        }
        return userDelegateRepository.queryDelegate(userId, status, options);
    }

    public void delegate(Long delegatorId, Long delegatedId)
        throws DelegateSelfException {
        if (delegatorId.equals(delegatedId)) {
            throw new DelegateSelfException();
        }

        UserDelegateDTO exist = userDelegateRepository.queryDelegate(delegatorId, delegatedId, null);
        if (exist != null) {
            if (DelegateStatusEnum.PENDING.name().equals(exist.getStatus())
                || DelegateStatusEnum.ACCEPTED.name().equals(exist.getStatus())) {
                return;
            }
            UserDelegateDTO update = new UserDelegateDTO();
            update.setId(exist.getId());
            update.setStatus(DelegateStatusEnum.PENDING.name());
            userDelegateRepository.update(update);
            return;
        }

        UserDelegateDTO insert = new UserDelegateDTO();
        insert.setDelegatorId(delegatorId);
        insert.setDelegatedId(delegatedId);
        insert.setStatus(DelegateStatusEnum.PENDING.name());
        userDelegateRepository.delegate(insert);
    }

    public void accept(Long delegatorId, Long delegatedId) throws DelegateNotFoundException {
        UserDelegateDTO exist = userDelegateRepository.queryDelegate(delegatorId, delegatedId, null);
        if (exist == null || !DelegateStatusEnum.PENDING.name().equals(exist.getStatus())) {
            throw new DelegateNotFoundException();
        }
        UserDelegateDTO update = new UserDelegateDTO();
        update.setId(exist.getId());
        update.setStatus(DelegateStatusEnum.ACCEPTED.name());
        userDelegateRepository.update(update);
    }

    public void revoke(Long delegatorId, Long delegatedId) throws DelegateNotFoundException {
        UserDelegateDTO exist = userDelegateRepository.queryDelegate(delegatorId, delegatedId, null);
        if (exist == null || DelegateStatusEnum.REVOKE.name().equals(exist.getStatus())) {
            throw new DelegateNotFoundException();
        }
        UserDelegateDTO update = new UserDelegateDTO();
        update.setId(exist.getId());
        update.setStatus(DelegateStatusEnum.REVOKE.name());
        userDelegateRepository.update(update);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserDisabled(UserDisabledEvent event) {
        try {
            userDelegateRepository.revokeAllInvolving(event.userId());
            log.info("revoked delegate relations for disabled userId={}", event.userId());
        } catch (Exception e) {
            log.error("failed to revoke delegates for disabled userId={}", event.userId(), e);
        }
    }
}
