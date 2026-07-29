package com.flydeer.structmind.service.id;

import com.flydeer.structmind.repository.mapper.UserMapper;
import com.flydeer.structmind.service.config.AppAuthProperties;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class IdGenerator {

    private final UserMapper userMapper;
    private final AppAuthProperties properties;

    public IdGenerator(UserMapper userMapper, AppAuthProperties properties) {
        this.userMapper = userMapper;
        this.properties = properties;
    }

    public synchronized long nextUserId() {
        Long max = userMapper.selectMaxId();
        long start = properties.getId().getStart();
        int min = properties.getId().getStepMin();
        int maxStep = properties.getId().getStepMax();
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
