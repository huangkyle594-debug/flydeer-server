package com.flydeer.structmind.repository.mysql.repository;

import com.flydeer.structmind.contract.user.enums.DelegateStatus;
import com.flydeer.structmind.repository.mysql.entity.UserDelegateEntity;
import com.flydeer.structmind.repository.mysql.entity.UserDelegateEntityExample;
import com.flydeer.structmind.repository.mysql.mapper.UserDelegateMapper;
import com.flydeer.structmind.repository.mysql.option.user.UserOptions;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class UserDelegateRepository {

    private final UserDelegateMapper userDelegateMapper;

    public List<Long> selectAcceptedGrantorIds(Long userId, UserOptions options) {
        UserDelegateEntityExample example = new UserDelegateEntityExample();
        UserDelegateEntityExample.Criteria criteria = example.createCriteria()
            .andGrantedUserIdEqualTo(userId);
        if (options.hasOnlyAcceptGrantedIds()) {
            criteria.andStatusEqualTo(DelegateStatus.ACCEPTED.name());
        }
        return userDelegateMapper.selectByExample(example).stream()
            .map(UserDelegateEntity::getUserId)
            .toList();
    }
}
