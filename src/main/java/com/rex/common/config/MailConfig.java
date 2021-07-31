package com.rex.common.config;

import cn.hutool.extra.mail.MailAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基于hu_tool的邮件账号配置
 */
@Configuration
public class MailConfig {

    @Value("${mail.host}")
    private String HOST;

    @Value("${mail.port}")
    private Integer PORT;

    @Value("${mail.network}")
    private String NETWORK;

    @Value("${mail.user}")
    private String USER;

    @Value("${mail.pass}")
    private String PASS;

    @Bean
    MailAccount mailAccount() {
        MailAccount account = new MailAccount();
        account.setHost(HOST);
        account.setPort(PORT);
        account.setAuth(true);
        account.setFrom(NETWORK);
        account.setUser(USER);
        account.setPass(PASS);
        return account;
    }
}
