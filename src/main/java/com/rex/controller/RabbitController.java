package com.rex.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rex.common.code.ApiMessage;
import io.swagger.annotations.Api;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Api(value = "消息队列")
@RestController
@RequestMapping("/mq")
public class RabbitController {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @PutMapping("/send")
    public ApiMessage<?> sendMsg (@RequestBody String data){
        JSONObject obj = JSON.parseObject(data);
        String queue = obj.getString("queue");
        Map<String, Object> map = obj.getJSONObject("map");
        rabbitTemplate.convertAndSend(queue, map);
        return new ApiMessage<>("success");
    }
}