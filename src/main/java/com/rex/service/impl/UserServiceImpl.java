package com.rex.service.impl;

import com.rex.bean.User;
import com.rex.common.util.redis.Cache;
import com.rex.common.util.redis.CacheDel;
import com.rex.mapper.UserMapper;
import com.rex.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service("userService")
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Cache(key = "user-")
    @Override
    public User findById(int id) {
        User user = userMapper.findById(id);
        log.info(user.toString());
        return user;
    }

    @CacheDel(key = "user-")
    @Override
    public void delCache(int id) {
        System.out.println("delCache-id=" + id);
    }

    @Override
    public Integer getUser(String name, String pass) {
        return userMapper.getUser(name, pass);
    }

    @Override
    public List<User> findByIds(List<Integer> list) {
        return userMapper.findByIds(list);
    }

    @Override
    public List<User> findByName(String name) {
        return userMapper.findByName(name);
    }

    @Override
    public List<User> query(User user) {
        return userMapper.query(user);
    }

}
