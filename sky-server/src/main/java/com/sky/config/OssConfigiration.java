package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jing Beier
 * @version 1.0
 * @function 配置类用于创建AliOssUtil对象
 * @date 2026/3/31 18:43
 */
@Configuration
@Slf4j
public class OssConfigiration {
    // 没加Bean注解，方法没有办法被调用到，加后项目启动会调用创建对象并由spring容器管理
    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){

        log.info("开始创建阿里云文件上传工具类对象:{}", aliOssProperties);

        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }
}
