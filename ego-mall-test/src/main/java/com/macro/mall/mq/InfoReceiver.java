package com.macro.mall.mq;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-05-13 10:03
 **/
@Component
@RabbitListener(
        bindings = @QueueBinding(
                value = @Queue(value = "log.info", autoDelete = "true"),
                exchange = @Exchange(value = "log.direct.topic", type = ExchangeTypes.TOPIC, autoDelete = "true"),
                key = "*.log.info.routing.key"
        )
)
public class InfoReceiver {
    @RabbitHandler()
    public void hander(String msg) {
        System.out.println("INFO receiver-->" + msg);
    }

}