package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 店铺营业状态定时任务：
 * 每天 06:00 自动营业（status=1），每天 23:00 自动打烊（status=0）。
 * 状态存在 Redis 的 shop:status 键中，与 ShopController 读写一致。
 */
@Component
@Slf4j
public class ShopTask {

    /** 营业状态在 Redis 中的键名（与 ShopController 保持一致） */
    private static final String SHOP_STATUS_KEY = "shop:status";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 每天早上 6 点自动营业
     * cron 表达式：秒 分 时 日 月 星期 → 0 0 6 * * ? = 每天 06:00:00
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void autoOpen() {
        log.info("定时任务：自动营业");
        redisTemplate.opsForValue().set(SHOP_STATUS_KEY, 1);
    }

    /**
     * 每天晚上 11 点自动打烊
     * cron 表达式：0 0 23 * * ? = 每天 23:00:00
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void autoClose() {
        log.info("定时任务：自动打烊");
        redisTemplate.opsForValue().set(SHOP_STATUS_KEY, 0);
    }
}
