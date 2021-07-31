package com.rex.controller;

import com.rex.bean.User;
import com.rex.bean.dto.UserDto;
import com.rex.common.code.ApiMessage;
import com.rex.common.code.ResCodeData;
import com.rex.common.jwt.JwtToken;
import com.rex.common.jwt.JwtUtil;
import com.rex.common.util.redis.Sign;
import com.rex.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 登录并获取token
     *
     * @param user
     * @return
     */
    @ApiOperation(value = "用户登录接口")
    @JwtToken
    @PostMapping("/login")
    public ApiMessage<?> login(@RequestBody UserDto user) {
        Map<String, Object> map = new HashMap<>();
        Integer userId = userService.getUser(user.getName(), user.getPassword());
        if (userId == null) {
            return new ApiMessage<>(ResCodeData.LOGIN_ERROR);
        } else {
            // 生成签名
            String token = JwtUtil.sign(String.valueOf(userId));
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("userId", String.valueOf(userId));
            userInfo.put("userName", user.getName());
            map.put("token", token);
            map.put("user", userInfo);
        }
        return new ApiMessage<>(map);
    }

    /**
     * 根据id获取用户信息
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/findById")
    public ApiMessage<?> findById(@RequestParam int id) {
        return new ApiMessage<>(userService.findById(id));
    }

    /**
     * 去除redis cache(测试，不处理数据库)
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/delCache")
    public ApiMessage<?> delCache(@RequestParam int id) {
        userService.delCache(id);
        return new ApiMessage<>("success");
    }

    /**
     * 用户签到
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/sign")
    public ApiMessage<?> sign(@RequestParam int id) {
        Sign.doSign(id, LocalDate.now());
        return new ApiMessage<>("success");
    }

    /**
     * 根据id获取用户信息
     *
     * @return
     */
    @GetMapping(value = "/findByIds")
    public ApiMessage<?> findByIds() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        return new ApiMessage<>(userService.findByIds(list));
    }

    /**
     * 根据name获取用户信息
     *
     * @return
     */
    @GetMapping(value = "/findByName")
    public ApiMessage<?> findByName(@RequestParam String name) {
        return new ApiMessage<>(userService.findByName(name));
    }

    /**
     * 根据name pw获取用户信息
     *
     * @return
     */
    @GetMapping(value = "/query")
    public ApiMessage<?> query(@RequestParam String name, @RequestParam String pw) {
        User user = new User(name, pw);
        return new ApiMessage<>(userService.query(user));
    }

}
