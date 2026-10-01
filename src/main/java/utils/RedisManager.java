package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Set;

/**
 * Thread-safe Redis cache manager with graceful fallback when Redis is offline.
 */
public class RedisManager {
    private static final Logger logger = LoggerFactory.getLogger(RedisManager.class);

    private static JedisPool jedisPool;
    private static boolean enabled = false;
    private static boolean available = false;
    private static int defaultTtlSeconds = 300;

    static {
        init();
    }

    public static synchronized void init() {
        enabled = EnvConfig.getBoolean("REDIS_ENABLED", true);
        defaultTtlSeconds = EnvConfig.getInt("REDIS_TTL_SECONDS", 300);

        if (!enabled) {
            logger.info("Redis cache is explicitly disabled via configuration.");
            available = false;
            return;
        }

        String host = EnvConfig.get("REDIS_HOST", "localhost");
        int port = EnvConfig.getInt("REDIS_PORT", 6379);
        String password = EnvConfig.get("REDIS_PASSWORD", null);
        int timeout = EnvConfig.getInt("REDIS_TIMEOUT", 2000);

        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(16);
            poolConfig.setMaxIdle(8);
            poolConfig.setMinIdle(2);
            poolConfig.setTestOnBorrow(true);

            if (password != null && !password.trim().isEmpty()) {
                jedisPool = new JedisPool(poolConfig, host, port, timeout, password);
            } else {
                jedisPool = new JedisPool(poolConfig, host, port, timeout);
            }

            // Test connection
            try (Jedis jedis = jedisPool.getResource()) {
                String ping = jedis.ping();
                if ("PONG".equalsIgnoreCase(ping)) {
                    available = true;
                    logger.info("Redis cache successfully connected to {}:{}", host, port);
                }
            }
        } catch (Exception e) {
            available = false;
            logger.warn("Redis is not available at {}:{}. Caching will be bypassed gracefully: {}", host, port, e.getMessage());
        }
    }

    public static boolean isAvailable() {
        return enabled && available && jedisPool != null && !jedisPool.isClosed();
    }

    public static String get(String key) {
        if (!isAvailable()) return null;
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            logger.debug("Redis GET failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    public static void set(String key, String value) {
        set(key, value, defaultTtlSeconds);
    }

    public static void set(String key, String value, int ttlSeconds) {
        if (!isAvailable()) return;
        try (Jedis jedis = jedisPool.getResource()) {
            if (ttlSeconds > 0) {
                jedis.setex(key, ttlSeconds, value);
            } else {
                jedis.set(key, value);
            }
        } catch (Exception e) {
            logger.debug("Redis SET failed for key {}: {}", key, e.getMessage());
        }
    }

    public static void del(String key) {
        if (!isAvailable()) return;
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        } catch (Exception e) {
            logger.debug("Redis DEL failed for key {}: {}", key, e.getMessage());
        }
    }

    public static void deleteByPrefix(String prefix) {
        if (!isAvailable()) return;
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> keys = jedis.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
            }
        } catch (Exception e) {
            logger.debug("Redis DEL prefix {} failed: {}", prefix, e.getMessage());
        }
    }

    public static void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }
}
