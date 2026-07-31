package com.flydeer.structmind.service.user.utils;

import com.flydeer.structmind.repository.mysql.mapper.UserInfoMapper;

import java.util.concurrent.ThreadLocalRandom;

import com.flydeer.structmind.service.user.config.IdGenerateConfig;
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
        Long max = userInfoMapper.selectMaxId();
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
