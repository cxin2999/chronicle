package com.cxin.chronicle.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/check")
    @Operation(summary = "健康检查")
    public String check() {
        StringBuilder result = new StringBuilder();
        boolean allHealthy = true;

        // 检查MySQL连接
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                log.info("MySQL连接正常");
            } else {
                result.append("MySQL连接异常: 连接已关闭; ");
                allHealthy = false;
                log.error("MySQL连接异常: 连接已关闭");
            }
        } catch (Exception e) {
            String errorMsg = "MySQL连接异常: " + e.getMessage();
            result.append(errorMsg).append("; ");
            allHealthy = false;
            log.error(errorMsg, e);
        }

        // 检查Redis连接
        try {
            String testKey = "health_check_test_" + System.currentTimeMillis();
            stringRedisTemplate.opsForValue().set(testKey, "test", Duration.ofSeconds(10));
            String value = stringRedisTemplate.opsForValue().get(testKey);
            if ("test".equals(value)) {
                log.info("Redis连接正常");
            } else {
                result.append("Redis连接异常: 数据验证失败; ");
                allHealthy = false;
                log.error("Redis连接异常: 数据验证失败");
            }
        } catch (Exception e) {
            String errorMsg = "Redis连接异常: " + e.getMessage();
            result.append(errorMsg).append("; ");
            allHealthy = false;
            log.error(errorMsg, e);
        }

        // 如果都正常，返回OK；否则返回错误信息
        if (allHealthy) {
            return "OK";
        } else {
            return result.toString().trim();
        }
    }
}
