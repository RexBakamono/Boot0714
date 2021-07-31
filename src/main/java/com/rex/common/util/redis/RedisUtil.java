package com.rex.common.util.redis;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;

/**
 * redis工具
 * springboot中@Value只能给普通变量赋值，不能给静态变量赋值
 * 使用@Component
 * 使用@PostConstruct注解修饰init非静态方法
 * 使用@PropertySource注解指定配置文件路径
 */
@EnableCaching
@Slf4j
@Component
@PropertySource("classpath:application.yml")
public class RedisUtil {

    private static JedisPool jedisPool = null;

    @Value("${spring.redis.host}")
    private String HOST;

    @Value("${spring.redis.port}")
    private int PORT;

    @Value("${spring.redis.password}")
    private String AUTH;

    @Value("${spring.redis.jedis.pool.max-active}")
    private int MAX_ACTIVE;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int MAX_IDLE;

    @Value("${spring.redis.jedis.pool.min-idle}")
    private int MIN_IDLE;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private int MAX_WAIT;

    @Value("${spring.redis.timeout}")
    private int TIMEOUT;

    /**
     * Jedis实例获取返回码
     */
    public static class JedisStatus {
        /**
         * Jedis实例获取失败
         */
        public static final long FAIL_LONG = -5L;
        /**
         * Jedis实例获取失败
         */
        public static final int FAIL_INT = -5;
        /**
         * Jedis实例获取失败
         */
        public static final String FAIL_STRING = "-5";
    }

    @PostConstruct
    private void initialPool() {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            //最大连接数，如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
            config.setMaxTotal(MAX_ACTIVE);
            //最大空闲数，控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
            config.setMaxIdle(MAX_IDLE);
            //最小空闲数
            config.setMinIdle(MIN_IDLE);
            //是否在从池中取出连接前进行检验，如果检验失败，则从池中去除连接并尝试取出另一个
            config.setTestOnBorrow(true);
            //在return给pool时，是否提前进行validate操作
            config.setTestOnReturn(true);
            //在空闲时检查有效性，默认false
            config.setTestWhileIdle(true);
            //表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；
            //这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义
            config.setMinEvictableIdleTimeMillis(30000);
            //表示idle object evitor两次扫描之间要sleep的毫秒数
            config.setTimeBetweenEvictionRunsMillis(60000);
            //表示idle object evitor每次扫描的最多的对象数
            config.setNumTestsPerEvictionRun(1000);
            //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
            config.setMaxWaitMillis(MAX_WAIT);

            if (StringUtils.isNotBlank(AUTH)) {
                jedisPool = new JedisPool(config, HOST, PORT, TIMEOUT, AUTH);
            } else {
                jedisPool = new JedisPool(config, HOST, PORT, TIMEOUT);
            }
        } catch (Exception e) {
            if (jedisPool != null) {
                jedisPool.close();
            }
            log.error("初始化Redis连接池失败", e);
        }
    }

    /**
     * 同步获取Jedis实例
     *
     * @return Jedis
     */
    public static Jedis getJedis() {
        Jedis jedis = null;
        try {
            if (jedisPool != null) {
                jedis = jedisPool.getResource();
            }
        } catch (Exception e) {
            log.error("同步获取Jedis实例失败" + e.getMessage(), e);
            jedis.close();
        }
        return jedis;
    }

    /**
     * 释放jedis资源(过时，jedis3 后采用jedis.close())
     *
     * @param jedis
     */
    public static void returnResource(final Jedis jedis) {
        if (jedis != null && jedisPool != null) {
            jedisPool.returnResource(jedis);
        }
    }

    public static void returnBrokenResource(final Jedis jedis) {
        if (jedis != null && jedisPool != null) {
            jedisPool.returnBrokenResource(jedis);
        }
    }

    /**
     * 设置缓存
     *
     * @param key
     * @param value
     */
    public static void setCache(String key, String value) {
        Jedis jedis = getJedis();
        jedis.set(key, value);
        jedis.close();
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public static void delCache(String key) {
        Jedis jedis = getJedis();
        jedis.del(key);
        jedis.close();
    }
}