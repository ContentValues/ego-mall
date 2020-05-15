package com.macro.mall.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-05-13 10:04
 **/
@Configuration
public class QueenConfig {
    @Bean
    public Queue createQueue() {
        return new Queue("hello-queue");
    }
}