package com.rex.common.util.redis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import redis.clients.jedis.Jedis;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Sign {

    public static boolean doSign(int userId, LocalDate date) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            int offset = date.getDayOfMonth() - 1;
            return jedis.setbit(buildSignKey(userId, date), offset, true);
        }
    }

    public static boolean checkSign(int userId, LocalDate date) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            int offset = date.getDayOfMonth() - 1;
            return jedis.getbit(buildSignKey(userId, date), offset);
        }
    }

    public long getSignCount(int userId, LocalDate date) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            return jedis.bitcount(buildSignKey(userId, date));
        }
    }

    /**
     * 连续天数
     *
     * @param date      时间
     * @param userId    用户id
     * @param signCount 天数，默认0
     * @param offset    偏移，默认0
     * @param isToMonth 是否当月
     * @return
     */
    public static int getContinueCount(LocalDate date, int userId, int signCount, int offset, boolean isToMonth) {
        int temCount = 0;
        Jedis jedis = RedisUtil.getJedis();
        DateTime lastTime = DateUtil.offsetMonth(new Date(), offset);
        String lastDayStr = null;
        int lastDays = 0;
        if (offset == 0) {
            // 查找当天是否签到
            boolean flag = jedis.getbit(buildSignKey(userId, date), date.getDayOfMonth() - 1);
            if (!flag) {
                lastDays = date.getDayOfMonth() - 1;
            } else {
                lastDays = date.getDayOfMonth();
            }
        } else {
            lastDays = getDaysOfMonth(lastTime);
        }
        if (lastDays != 0) {
            lastDayStr = String.format("u%d", lastDays);
            List<Long> lastList = jedis.bitfield(buildSignKey(userId, dateToLocalDate(lastTime)), "GET", lastDayStr, "0");
            if (CollUtil.isNotEmpty(lastList)) {
                long v = lastList.get(0) == null ? 0 : lastList.get(0);
                for (int i = 0; i < getDaysOfMonth(lastTime); i++) {
                    if (v >> 1 << 1 == v) {
                        break;
                    } else {
                        signCount += 1;
                        temCount += 1;
                    }
                    v >>= 1;
                }
            }
        }
        if (temCount == lastDays && !isToMonth) {
            signCount = getContinueCount(date, userId, signCount, offset - 1, false);
        }
        jedis.close();
        return signCount;
    }

    private static String formatDate(LocalDate date) {
        return formatDate(date, "yyyyMM");
    }

    private static String formatDate(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    private static String buildSignKey(int uid, LocalDate date) {
        return String.format("u:sign:%d:%s", uid, formatDate(date));
    }

    /**
     * 获取Date类型的当月的天数
     */
    private static int getDaysOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Date类型转换成LocalDate
     */
    private static LocalDate dateToLocalDate(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
        return LocalDate.from(localDateTime);
    }

}