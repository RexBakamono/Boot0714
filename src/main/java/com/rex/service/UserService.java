package com.rex.service;

import com.rex.bean.User;

import java.util.List;

public interface UserService {

    User findById(int id);

    void delCache(int id);

    Integer getUser(String name, String pass);

    List<User> findByIds(List<Integer> list);

    List<User> findByName(String name);

    List<User> query(User user);
}
