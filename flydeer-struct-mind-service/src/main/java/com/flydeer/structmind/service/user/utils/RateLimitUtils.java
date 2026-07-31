package com.flydeer.structmind.service.user.utils;

import com.flydeer.structmind.common.exception.ratelimit.LoginRateLimitException;
import com.flydeer.structmind.common.exception.ratelimit.SmsRateLimitException;
import com.flydeer.structmind.service.user.config.RateLimitConfig;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitUtils {

    private final RateLimitConfig rateLimitConfig;
    private final Cache<String, Long> intervalCache;
    private final Cache<String, AtomicInteger> dailyCache;

    public RateLimitUtils(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
        this.intervalCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofDays(1)).build();
        this.dailyCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofDays(1)).build();
    }

    public void checkSms(String phone, String ip) throws SmsRateLimitException {
        try {
            checkInterval("sms:phone:" + phone, rateLimitConfig.getSmsInterval());
            checkDaily("sms:day:phone:" + phone, rateLimitConfig.getSmsDailyLimitPerPhone());
            checkDaily("sms:day:ip:" + ip, rateLimitConfig.getSmsDailyLimitPerIp());
        } catch (Exception e) {
            throw new SmsRateLimitException();
        }
    }

    public void checkLogin(String key) throws LoginRateLimitException {
        try {
            checkInterval("login:" + key, rateLimitConfig.getLoginInterval());
        } catch (Exception e) {
            throw new LoginRateLimitException();
        }
    }

    private void checkInterval(String key, Duration interval) {
        long now = System.currentTimeMillis();
        Long last = intervalCache.getIfPresent(key);
        Assert.isTrue(last == null || now - last > interval.toMillis(), "too many requests");
        intervalCache.put(key, now);
    }

    private void checkDaily(String key, int limit) {
        AtomicInteger counter = dailyCache.get(key, k -> new AtomicInteger(0));
        Assert.isTrue(counter.incrementAndGet() < limit, "daily limit exceeded");
    }
}
