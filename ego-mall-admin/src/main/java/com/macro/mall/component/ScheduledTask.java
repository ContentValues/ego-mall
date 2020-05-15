package com.macro.mall.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @program: mall
 * @author: ShyBlue
 * @create: 2020-04-29 10:54
 **/
@Component
@EnableScheduling
public class ScheduledTask {

    Logger logger = LoggerFactory.getLogger(ScheduledTask.class);

    /**
     * cron表达式：Seconds Minutes Hours DayofMonth Month DayofWeek [Year]
     * 每2秒扫描一次
     */
    @Scheduled(cron = "0/2 * * ? * ?")
    public void shceduledOutLog() {
//        logger.info("测试信息333");
    }

}