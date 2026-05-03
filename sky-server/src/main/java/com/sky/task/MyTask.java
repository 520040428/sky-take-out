package com.sky.task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Jing Beier
 * @version 1.0
 * @function
 * @date
 */

/**
 * 自定义定时任务类
 */
@Component
@Slf4j
public class MyTask {

//    @Scheduled(cron = "0/5 * * * * ?")
    public void exectueTask(){
        log.info("定时任务开始执行: {}", new Date());
    }
}
