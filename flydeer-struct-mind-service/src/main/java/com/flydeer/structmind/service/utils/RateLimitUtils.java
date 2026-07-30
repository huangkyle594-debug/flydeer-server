package com.flydeer.structmind.service.utils;

import com.flydeer.structmind.common.error.ErrorCodes;
import com.flydeer.structmind.common.exception.business.BusinessException;
import com.flydeer.structmind.service.config.AppAuthProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class RateLimitUtils {

    private final AppAuthProperties properties;
    private final Cache<String, Long> intervalCache;
    private final Cache<String, AtomicInteger> dailyCache;

    public RateLimitUtils(AppAuthProperties properties) {
        this.properties = properties;
        this.intervalCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofDays(1)).build();
        this.dailyCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofDays(1)).build();
    }

    public void checkSms(String phone, String ip) {
        checkInterval("sms:phone:" + phone, properties.getRateLimit().getSmsInterval());
        checkDaily("sms:day:phone:" + phone, properties.getRateLimit().getSmsDailyLimitPerPhone());
        checkDaily("sms:day:ip:" + ip, properties.getRateLimit().getSmsDailyLimitPerIp());
    }

    public void checkLogin(String key) {
        checkInterval("login:" + key, properties.getRateLimit().getLoginInterval());
    }

    private void checkInterval(String key, Duration interval) {
        long now = System.currentTimeMillis();
        Long last = intervalCache.getIfPresent(key);
        if (last != null && now - last < interval.toMillis()) {
            throw new BusinessException(ErrorCodes.TOO_MANY_REQUESTS, "too many requests");
        }
        intervalCache.put(key, now);
    }

    private void checkDaily(String key, int limit) {
        AtomicInteger counter = dailyCache.get(key, k -> new AtomicInteger(0));
        if (counter.incrementAndGet() > limit) {
            throw new BusinessException(ErrorCodes.TOO_MANY_REQUESTS, "daily limit exceeded");
        }
    }
}
