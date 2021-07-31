package com.rex.common.config;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
// 开启定时任务
@EnableScheduling
// 开启多线程
@EnableAsync
public class TaskConfig {

    @Async
    @Scheduled(fixedDelay = 1000 * 60 * 5)
    public void first() {
        System.out.println("第一个定时任务开始 : " + LocalDateTime.now().toLocalTime() + "\r\n线程 : " + Thread.currentThread().getName());
        System.out.println();
    }

    @Async
    @Scheduled(fixedDelay = 2000 * 60 * 5)
    public void second() {
        System.out.println("第二个定时任务开始 : " + LocalDateTime.now().toLocalTime() + "\r\n线程 : " + Thread.currentThread().getName());
        System.out.println();
    }

}