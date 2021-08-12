package com.rex.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(description = "邮件")
@Slf4j
@RestController
@RequestMapping("/mail")
public class MailController {

    @Resource
    private MailAccount mailAccount;

    /**
     * 普通邮件
     *
     * @param data
     */
    @PostMapping("/send")
    public void send(@RequestBody String data) {
        JSONObject obj = JSON.parseObject(data);
        String account = obj.getString("account");
        String title = obj.getString("title");
        String content = obj.getString("content");
        boolean isHtml = obj.getBoolean("isHtml");
        MailUtil.send(mailAccount, account, title, content, isHtml);
    }

    /**
     * 带附件邮件
     *
     * @param data
     */
    @PostMapping("/sendWithFile")
    public void sendWithFile(@RequestBody String data) {
        JSONObject obj = JSON.parseObject(data);
        String account = obj.getString("account");
        String title = obj.getString("title");
        String content = obj.getString("content");
        boolean isHtml = obj.getBoolean("isHtml");
        String filePath = obj.getString("filePath");
        MailUtil.send(mailAccount, account, title, content, isHtml, FileUtil.file(filePath));
    }

    /**
     * 批量发送
     *
     * @param data
     */
    @PostMapping("/bulkSend")
    public void bulkSend(@RequestBody String data) {
        JSONObject obj = JSON.parseObject(data);
        List<String> account = JSON.parseArray(obj.getString("account")).toJavaList(String.class);
        String title = obj.getString("title");
        String content = obj.getString("content");
        boolean isHtml = obj.getBoolean("isHtml");
        MailUtil.send(mailAccount, account, title, content, isHtml);
    }


}
