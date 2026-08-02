package com.flydeer.repository.mysql.repository;

import com.flydeer.repository.mysql.dto.UserDelegateDTO;
import com.flydeer.repository.mysql.entity.UserDelegateEntity;
import com.flydeer.repository.mysql.entity.UserDelegateEntityExample;
import com.flydeer.repository.mysql.mapper.UserDelegateMapper;
import com.flydeer.repository.mysql.mapping.UserDelegateMapping;
import com.flydeer.repository.mysql.option.user.UserOptions;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class UserDelegateRepository {

    private final UserDelegateMapper userDelegateMapper;

    public List<UserDelegateDTO> queryDelegate(Long userId, List<String> status, UserOptions options) {
        UserDelegateEntityExample example = new UserDelegateEntityExample();
        UserDelegateEntityExample.Criteria criteria = example.createCriteria();

        if (options.hasDelegated()) {
            criteria.andDelegatedIdEqualTo(userId);
        } else {
            criteria.andDelegatorIdEqualTo(userId);
        }
        if (status != null) {
            criteria.andStatusIn(status);
        }

        return userDelegateMapper.selectByExample(example).stream().map(UserDelegateMapping.INSTANCE::toDto).toList();
    }

    public UserDelegateDTO queryDelegate(Long delegatorId, Long delegatedId, String status) {
        UserDelegateEntityExample example = new UserDelegateEntityExample();
        UserDelegateEntityExample.Criteria criteria = example.createCriteria()
            .andDelegatorIdEqualTo(delegatorId)
            .andDelegatedIdEqualTo(delegatedId);
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
