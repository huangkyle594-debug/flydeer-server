package com.flydeer.repository.mysql.utils;

import com.flydeer.repository.mysql.config.IdGenerateConfig;
import com.flydeer.repository.mysql.entity.UserInfoEntity;
import com.flydeer.repository.mysql.entity.UserInfoEntityExample;
import com.flydeer.repository.mysql.mapper.UserInfoMapper;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class IdGenerateUtils {

    private final UserInfoMapper userInfoMapper;
    private final IdGenerateConfig idGenerateConfig;

    public IdGenerateUtils(UserInfoMapper userInfoMapper,
                           IdGenerateConfig idGenerateConfig) {
        this.userInfoMapper = userInfoMapper;
        this.idGenerateConfig=idGenerateConfig;
    }

    public synchronized long nextUserId() {
        UserInfoEntityExample example = new UserInfoEntityExample();
        example.setOrderByClause("`id` desc");
        List<UserInfoEntity> rows = userInfoMapper.selectByExample(example);
        Long max = rows.isEmpty() ? null : rows.getFirst().getId();
        long start = idGenerateConfig.getStart();
        int min = idGenerateConfig.getStepMin();
        int maxStep = idGenerateConfig.getStepMax();
        if (maxStep < min) {
            maxStep = min;
        }
        int step = ThreadLocalRandom.current().nextInt(min, maxStep + 1);
        if (max == null || max < start) {
            return start;
        }
        return max + step;
    }
}
