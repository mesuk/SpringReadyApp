package com.appcoder.springreadyapp.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Collections;

@Service
public class RedisCacheRepositoryImpl implements RedisCacheRepository {

    private static Log log = LogFactory.getLog(RedisCacheRepositoryImpl.class);
    private static String SET_SUCCESS = "OK";
    private static String KEY_PRE = "REDIS_LOCK_";
    private Integer dbIndex = 15;

    private Integer lockExpireTimeSeconds = 120 * 1000;

    JedisConnectionFactory jedisConnectionFactory;
    JedisConnection jedisConnection;


    public RedisCacheRepositoryImpl(JedisConnectionFactory jedisConnectionFactory) {
        this.jedisConnectionFactory = jedisConnectionFactory;
    }


    @Override
    public boolean isTransactionRunningForSender(String key) {
        Jedis jedis = null;
        try {
            jedis = getRedis();
            key = KEY_PRE + key;
            String value = fetchLockValue(key);
            if (jedis.get(key) != null) {
                log.info("Transaction Running for : " + key);
                return true;
            } else {
                log.info("Transaction not Running for : " + key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }


    @Override
    public void makeSenderBusyForTransactionNormal(String key) {
        Jedis jedis = null;
        try {
            jedis = getRedis();
            key = KEY_PRE + key;
            String value = fetchLockValue(key);
            if (SET_SUCCESS.equals(jedis.set(key, value))) {
                log.info("Sender made busy done for key : " + key + ",value : " + value);
            } else {
                log.info("Sender made busy failed for key : " + key + ",value : " + value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public void makeSenderFreeForTransactionNormal(String key) {
        Long RELEASE_SUCCESS = 1L;
        Jedis jedis = null;
        try {
            jedis = getRedis();
            key = KEY_PRE + key;
            String value = fetchLockValue(key);
            String command = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            if (RELEASE_SUCCESS.equals(jedis.eval(command, Collections.singletonList(key), Collections.singletonList(value)))) {
                log.info("Sender release done : " + key + ",value : " + value);
            } else {
                log.error("Sender release failed, possible cause key not acquired YET : " + key);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean acquireLock(String key) {
        Jedis jedis = null;
        try {
            jedis = getRedis();
            key = KEY_PRE + key;
            String value = fetchLockValue(key);
            if (SET_SUCCESS.equals(jedis.set(key, value, "NX", "EX", lockExpireTimeSeconds))) {
                log.info("Reids Lock acquire done key : " + key + ",value : " + value);
                return true;
            } else {
                log.error("Reids Lock acquire failed key, possible cause key already acquired : " + key + ",value : " + value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    @Override
    public boolean releaseLock(String key) {
        Long RELEASE_SUCCESS = 1L;
        Jedis jedis = null;
        try {
            jedis = getRedis();
            key = KEY_PRE + key;
            String value = fetchLockValue(key);
            String command = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            if (RELEASE_SUCCESS.equals(jedis.eval(command, Collections.singletonList(key), Collections.singletonList(value)))) {
                log.info("Reids Lock key release done : " + key + ",value : " + value);
                return true;
            } else {
                log.error("Reids Lock key release failed, possible cause key not acquired YET : " + key);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }


    private String fetchLockValue(String key) {
        return "VALUE_" + key;
    }

    private Jedis getRedis() {
        jedisConnection = (JedisConnection) jedisConnectionFactory.getConnection();
        Jedis jedis = jedisConnection.getJedis();
        jedis.select(dbIndex);
        return jedis;
    }
}
