package com.flydeer.structmind.repository.mysql.repository;

import com.flydeer.structmind.contract.user.enums.DelegateStatusEnum;
import com.flydeer.structmind.repository.mysql.dto.UserDelegateDTO;
import com.flydeer.structmind.repository.mysql.entity.UserDelegateEntity;
import com.flydeer.structmind.repository.mysql.entity.UserDelegateEntityExample;
import com.flydeer.structmind.repository.mysql.mapper.UserDelegateMapper;
import com.flydeer.structmind.repository.mysql.mapping.UserDelegateMapping;
import com.flydeer.structmind.repository.mysql.option.user.UserOptions;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class UserDelegateRepository {

    private final UserDelegateMapper userDelegateMapper;

    public List<UserDelegateDTO> queryGrantorIds(Long userId, UserOptions options) {
        UserDelegateEntityExample example = new UserDelegateEntityExample();
        UserDelegateEntityExample.Criteria criteria = example.createCriteria();

        if (options.hasGrantedUser()) {
            criteria.andGrantedUserIdEqualTo(userId);
        } else {
            criteria.andUserIdEqualTo(userId);
        }

        if (options.hasOnlyAcceptGrantedIds()) {
            criteria.andStatusEqualTo(DelegateStatusEnum.ACCEPTED.name());
        }

        return userDelegateMapper.selectByExample(example).stream().map(UserDelegateMapping.INSTANCE::toDto).toList();
    }

    public UserDelegateDTO queryDelegate(Long userId, Long grantedUserId, String status) {
        UserDelegateEntityExample example = new UserDelegateEntityExample();
        UserDelegateEntityExample.Criteria criteria = example.createCriteria()
            .andUserIdEqualTo(userId)
            .andGrantedUserIdEqualTo(grantedUserId);
        if (StringUtils.isNotBlank(status)) {
            criteria.andStatusEqualTo(status);
        }
        List<UserDelegateEntity> rows = userDelegateMapper.selectByExample(example);
        if (rows.isEmpty()) {
            return null;
        }
        return UserDelegateMapping.INSTANCE.toDto(rows.getFirst());
    }

    public void delegate(UserDelegateDTO dto, UserOptions options) {
        UserDelegateEntity entity = UserDelegateMapping.INSTANCE.dto2entity(dto);
        if (options.hasUpdateToNull()) {
            userDelegateMapper.insert(entity);
        } else {
            userDelegateMapper.insertSelective(entity);
        }
    }

    public void update(UserDelegateDTO dto, UserOptions options) {
        UserDelegateEntity entity = UserDelegateMapping.INSTANCE.dto2entity(dto);
        if (options.hasUpdateToNull()) {
            userDelegateMapper.updateByPrimaryKey(entity);
        } else {
            userDelegateMapper.updateByPrimaryKeySelective(entity);
        }
    }
}
