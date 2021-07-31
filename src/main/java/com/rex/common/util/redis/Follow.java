package com.rex.common.util.redis;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.*;

@Slf4j
public class Follow {

    /**
     * 关注
     *
     * @param userId
     * @param followUserId
     * @return 0 操作失败 1 已关注 2 互相关注
     */
    public static int follow(Integer userId, Integer followUserId) {
        Jedis jedis = RedisUtil.getJedis();
        Transaction transaction = jedis.multi();
        try {
            long time = new Date().getTime();
            transaction.zadd(userId + ":follow", time, String.valueOf(followUserId));
            transaction.zadd(followUserId + ":fans", time, String.valueOf(userId));
            transaction.exec();
            // 返回用户之间的关系
            boolean beFollow = jedis.zscore(followUserId + ":follow", String.valueOf(userId)) != null;
            if (beFollow) {
                return 2;
            }
            return 1;
        } catch (Exception e) {
            transaction.discard();
            log.error(userId + "关注失败" + followUserId);
        } finally {
            jedis.close();
        }
        return 0;
    }

    /**
     * 取消关注
     *
     * @param userId
     * @param followUserId
     * @return 0 操作失败 1 操作成功
     */
    public static int cancelFollow(Integer userId, Integer followUserId) {
        Jedis jedis = RedisUtil.getJedis();
        Transaction transaction = jedis.multi();
        try {
            transaction.zrem(userId + ":follow", String.valueOf(followUserId));
            transaction.zrem(followUserId + ":fans", String.valueOf(userId));
            transaction.exec();
            return 1;
        } catch (Exception e) {
            transaction.discard();
            log.error(userId + "取消关注失败" + followUserId);
        } finally {
            jedis.close();
        }
        return 0;
    }

    private static LinkedList getList(Jedis jedis, Set<String> sets, Integer userId) {
        LinkedList<Map<String, Object>> list = new LinkedList<>();
        for (String id : sets) {
            int status = 0;
            boolean follow = jedis.zscore(userId + ":follow", id) != null;
            boolean beFollow = jedis.zscore(id + ":follow", String.valueOf(userId)) != null;
            if (follow) {
                status = 1;
                if (beFollow) {
                    status = 2;
                }
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("status", status);
            list.add(map);
        }
        return list;
    }

    /**
     * 关注列表
     *
     * @param userId
     * @param startNo
     * @param pageSize
     * @return 0 未关注 1 已关注 2 互相关注
     */
    public static LinkedList followList(Integer userId, Integer startNo, Integer pageSize) {
        Jedis jedis = RedisUtil.getJedis();
        Set<String> sets = jedis.zrevrange(userId + ":follow", startNo, pageSize);
        return getList(jedis, sets, userId);
    }

    /**
     * 粉丝列表
     *
     * @param userId
     * @param startNo
     * @param pageSize
     * @return 0 未关注 1 已关注 2 互相关注
     */
    public static LinkedList fansList(Integer userId, Integer startNo, Integer pageSize) {
        Jedis jedis = RedisUtil.getJedis();
        Set<String> sets = jedis.zrevrange(userId + ":fans", startNo, pageSize);
        return getList(jedis, sets, userId);
    }

    /**
     * 获取用户关注关系
     *
     * @param userId
     * @param list
     * @return -1 自己 0 未关注 1 已关注 2 互相关注
     */
    public static List<Map<String, Object>> statusList(Integer userId, List<Map<String, Object>> list) {
        Jedis jedis = RedisUtil.getJedis();
        for (Map<String, Object> map : list) {
            int id = Integer.valueOf(map.get("userId").toString());
            int status = -1;
            if (userId != id) {
                status = 0;
                boolean follow = jedis.zscore(userId + ":follow", map.get("userId").toString()) != null;
                boolean beFollow = jedis.zscore(id + ":follow", String.valueOf(userId)) != null;
                if (follow) {
                    status = 1;
                    if (beFollow) {
                        status = 2;
                    }
                }
            }
            map.put("status", status);
        }
        return list;
    }

}
