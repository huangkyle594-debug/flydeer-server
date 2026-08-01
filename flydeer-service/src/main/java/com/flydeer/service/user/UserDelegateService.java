package com.flydeer.service.user;

import com.flydeer.common.exception.business.DelegateNotFoundException;
import com.flydeer.common.exception.request.DelegateSelfException;
import com.flydeer.contract.user.enums.DelegateRelationEnum;
import com.flydeer.contract.user.enums.DelegateStatusEnum;
import com.flydeer.repository.mysql.dto.UserDelegateDTO;
import com.flydeer.repository.mysql.option.user.UserOptions;
import com.flydeer.repository.mysql.repository.UserDelegateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserDelegateService {

    private final UserDelegateRepository userDelegateRepository;

    public void delegate(Long userId, Long grantedUserId)
        throws DelegateSelfException {
        if (userId.equals(grantedUserId)) {
            throw new DelegateSelfException();
        }

        UserDelegateDTO exist = userDelegateRepository.queryDelegate(userId, grantedUserId, null);
        if (exist != null) {
            if (DelegateStatusEnum.PENDING.name().equals(exist.getStatus())
                || DelegateStatusEnum.ACCEPTED.name().equals(exist.getStatus())) {
                return;
            }
            UserDelegateDTO update = new UserDelegateDTO();
            update.setId(exist.getId());
            update.setStatus(DelegateStatusEnum.PENDING.name());
            userDelegateRepository.update(update, UserOptions.option());
            return;
        }

        UserDelegateDTO insert = new UserDelegateDTO();
        insert.setUserId(userId);
        insert.setGrantedUserId(grantedUserId);
        insert.setStatus(DelegateStatusEnum.PENDING.name());
        userDelegateRepository.delegate(insert, UserOptions.option());
    }

    public void accept(Long userId, Long grantedUserId) throws DelegateNotFoundException {
        UserDelegateDTO exist = userDelegateRepository.queryDelegate(userId, grantedUserId, null);
        if (exist == null || !DelegateStatusEnum.PENDING.name().equals(exist.getStatus())) {
            throw new DelegateNotFoundException();
        }
        UserDelegateDTO update = new UserDelegateDTO();
        update.setId(exist.getId());
        update.setStatus(DelegateStatusEnum.ACCEPTED.name());
        userDelegateRepository.update(update, UserOptions.option());
    }

    public void revoke(Long userId, Long grantedUserId) throws DelegateNotFoundException {
        UserDelegateDTO exist = userDelegateRepository.queryDelegate(userId, grantedUserId, null);
        if (exist == null || DelegateStatusEnum.REVOKE.name().equals(exist.getStatus())) {
            throw new DelegateNotFoundException();
        }
        UserDelegateDTO update = new UserDelegateDTO();
        update.setId(exist.getId());
        update.setStatus(DelegateStatusEnum.REVOKE.name());
        userDelegateRepository.update(update, UserOptions.option());
    }

    public List<UserDelegateDTO> queryDelegations(
        Long userId, List<String> status, DelegateRelationEnum relation) {
        UserOptions options = UserOptions.option();
        if (DelegateRelationEnum.MANAGED.equals(relation)) {
            options.grantedUser();
        }
        return userDelegateRepository.queryGrantorIds(userId, status, options);
    }

    /**
     * Grantor userIds who have ACCEPTED delegating to {@code userId}.
     */
    public List<Long> listAcceptedGrantorIds(Long userId) {
        return queryDelegations(
            userId,
            List.of(DelegateStatusEnum.ACCEPTED.name()),
            DelegateRelationEnum.MANAGED)
            .stream()
            .map(UserDelegateDTO::getUserId)
            .toList();
    }
}
