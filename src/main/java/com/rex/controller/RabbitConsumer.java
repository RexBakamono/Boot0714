package com.rex.controller;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitConsumer {
    // 手动ack ackMode = "MANUAL"
    @RabbitListener(queues = {"demo"})
    @RabbitHandler
    public void consumer(JSONObject data, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) {
        System.out.println(data.toJSONString());
        try {
//            测试手动应答
//            Thread.currentThread().sleep(1000 * 15);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}