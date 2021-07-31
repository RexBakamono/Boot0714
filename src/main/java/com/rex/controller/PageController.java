package com.rex.controller;

import com.rex.bean.User;
import com.rex.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
// @RestController导致freemarker不会被处理
@Controller
@RequestMapping("/page")
public class PageController {

    @Resource
    private UserService userService;

    @RequestMapping("/")
    public String index(Model model) {
        List<Integer> ids = new ArrayList<>();
        ids.add(1);
        ids.add(2);
        List<User> ls = userService.findByIds(ids);
        model.addAttribute("ls", ls);

        List<Map<String, Object>> list = new ArrayList<>();
        for (User user : ls) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", user.getId());
            map.put("name", user.getName());
            map.put("pass", user.getPassword());
            list.add(map);
        }
        model.addAttribute("list", list);

        return "index";
    }
}