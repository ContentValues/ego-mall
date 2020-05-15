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
                value = @Queue(value = "sms", autoDelete = "true"),
                exchange = @Exchange(value = "log.direct.fanout", type = ExchangeTypes.FANOUT, autoDelete = "true")
        )
)
public class SMSReceiver {
    @RabbitHandler()
    public void hander(String msg) {
        System.out.println("SMSReceiver receiver-->" + msg);
    }

}