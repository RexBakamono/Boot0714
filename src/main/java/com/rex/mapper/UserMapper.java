package com.rex.mapper;

import com.rex.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "userMapper")
public interface UserMapper {

    User findById(int id);

    Integer getUser(String name, String pass);

    List<User> findByIds(List<Integer> list);

    List<User> findByName(String name);

    List<User> query(User user);

}
