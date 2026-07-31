package com.flydeer.structmind.service.user.utils;

import com.flydeer.structmind.repository.mysql.mapper.UserMapper;

import java.util.concurrent.ThreadLocalRandom;

import com.flydeer.structmind.service.user.config.IdGenerateConfig;
import org.springframework.stereotype.Component;

@Component
public class IdGenerateUtils {

    private final UserMapper userMapper;
    private final IdGenerateConfig idGenerateConfig;

    public IdGenerateUtils(UserMapper userMapper,
                           IdGenerateConfig idGenerateConfig) {
        this.userMapper = userMapper;
        this.idGenerateConfig=idGenerateConfig;
    }

    public synchronized long nextUserId() {
        Long max = userMapper.selectMaxId();
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
