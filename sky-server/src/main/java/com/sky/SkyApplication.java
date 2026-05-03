package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理@Transcational
@EnableCaching //开启缓存
@EnableScheduling //开启任务调度，能够使用Spring Task
@Slf4j
public class SkyApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SkyApplication.class, args);
//        log.info("server started");
//
//        Environment env = context.getEnvironment();
//
//        log.info("=== Redis 配置 ===");
//        log.info("Host: " + env.getProperty("spring.redis.host"));
//        log.info("Port: " + env.getProperty("spring.redis.port"));
//        log.info("Password: " + (env.getProperty("spring.redis.password") != null ? "已配置" : "未配置"));
    }
}
