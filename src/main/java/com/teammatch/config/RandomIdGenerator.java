package com.teammatch.config;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 自定义随机 ID 生成器
 * 用于 user 表，替代自增 ID，防止用户 ID 被枚举
 * 格式：时间戳(毫秒后6位) + 随机数(6位) = 12位数字
 */
@Slf4j
@Component
public class RandomIdGenerator implements IdentifierGenerator {

    /**
     * 生成随机 ID（12位数字，约 10^12 空间，碰撞概率极低）
     */
    @Override
    public Long nextId(Object entity) {
        long timestamp = System.currentTimeMillis() % 1000000;     // 后6位
        long random = (long) (Math.random() * 1000000);           // 随机6位
        long id = timestamp * 1000000 + random;
        // 确保 > 100000（6位），避免与旧自增 ID 冲突
        return Math.max(id, 100000 + (long)(Math.random() * 900000));
    }
}
