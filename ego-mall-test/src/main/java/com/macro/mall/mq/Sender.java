package com.macro.mall.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-05-13 10:03
 **/
@Component
public class Sender {

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void send(String msg) {
        //向消息队列发送消息
        //参数一：交换器名称。
        //参数二：路由键
        //参数三：消息
        this.rabbitTemplate.convertAndSend("log.direct.topic", "order.log.error.routing.key", msg + " ERROR");
        this.rabbitTemplate.convertAndSend("log.direct.topic", "order.log.info.routing.key", msg + " INFO");
    }


    public void sendFanout(String msg) {
        //向消息队列发送消息
        //参数一：交换器名称。
        //参数二：路由键
        //参数三：消息
        this.rabbitTemplate.convertAndSend("log.direct.fanout", "", msg + " ERROR");
    }

}