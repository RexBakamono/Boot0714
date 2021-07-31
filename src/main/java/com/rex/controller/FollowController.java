package com.rex.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rex.common.code.ApiMessage;
import com.rex.common.util.redis.Follow;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fans")
public class FollowController {

    @RequestMapping("/follow")
    public ApiMessage<?> follow(@RequestBody String params) {
        JSONObject obj = JSON.parseObject(params);
        int type = obj.getInteger("type");
        int userId = obj.getInteger("userId");
        int followUserId = obj.getInteger("followUserId");
        int code = 0;
        if (type == 0) {
            code = Follow.follow(userId, followUserId);
        } else if (type == 1) {
            code = Follow.cancelFollow(userId, followUserId);
        }
        return new ApiMessage<>(code);
    }

    @RequestMapping("/followList")
    public ApiMessage<?> followList(@RequestBody String params) {
        JSONObject obj = JSON.parseObject(params);
        int userId = obj.getInteger("userId");
        int size = obj.getInteger("size");
        int num = obj.getInteger("num");
        int no = (num - 1) * size;
        return new ApiMessage<>(Follow.followList(userId, no, size));
    }

    @RequestMapping("/fansList")
    public ApiMessage<?> fansList(@RequestBody String params) {
        JSONObject obj = JSON.parseObject(params);
        int userId = obj.getInteger("userId");
        int size = obj.getInteger("size");
        int num = obj.getInteger("num");
        int no = (num - 1) * size;
        return new ApiMessage<>(Follow.fansList(userId, no, size));
    }

}
