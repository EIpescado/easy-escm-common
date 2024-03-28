package org.group1418.easy.escm.common.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.func.Func0;
import cn.hutool.core.lang.func.VoidFunc0;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.group1418.easy.escm.common.serializer.CustomGenericFastJsonRedisSerializer;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author yq
 * @date 2020/10/16 09:07
 * @description 自定义redis过期时间缓存配置
 * @since V1.0.0
 */
public class CustomRedisCacheService {

    private final static Logger logger = LoggerFactory.getLogger(CustomRedisCacheService.class);
    /**
     * 缓存过期配置
     */
    private final static Map<String, CustomCacheConfig> CONFIG_MAP = new ConcurrentHashMap<>();
    private static final long DEFAULT_EXPIRE_SECONDS = -1L;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final RedisSerializer<String> keySerializer;
    private final CustomGenericFastJsonRedisSerializer valueSerializer;
    private static final String REDIS_LOCK_SUFFIX = "_LOCK_HASH";
    private static final String INCREMENT_AND_TTL_LUA = "local errorCount = redis.call('GET',KEYS[1])" +
            "\nif (errorCount == false) then errorCount = 0" +
            "\nelse errorCount = tonumber(errorCount) end" +
            "\nerrorCount = tonumber(errorCount) + tonumber(ARGV[2])" +
            "\nredis.call('SETEX',KEYS[1],ARGV[1],errorCount)" +
            "\nreturn errorCount";
    private static final String AUTO_INC_SN = "auto_inc_sn";

    public CustomRedisCacheService(RedisTemplate<String, Object> redisTemplate, RedissonClient redissonClient) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        //缓存redis的Serializer 参照RedisConfig中配置
        this.valueSerializer = (CustomGenericFastJsonRedisSerializer) redisTemplate.getDefaultSerializer();
        this.keySerializer = (StringRedisSerializer) redisTemplate.getKeySerializer();
    }

    /**
     * 生成redisKey
     *
     * @param keys 建
     * @return 值
     */
    public static String buildKey(Object... keys) {
        Assert.notEmpty(keys);
        return StrUtil.join(StrUtil.COLON, keys);
    }

    /**
     * 设置缓存
     *
     * @param redisKey   缓存key
     * @param ttlSeconds 过期时间单位秒
     * @param getter     数据获取
     * @param lockKey    锁键值 使得同一时间只有一条线程去设置缓存,防止缓存击穿
     * @return 需要的数据
     */
    public <T> T set(String redisKey, long ttlSeconds, RealDataGetter<T> getter, String lockKey) {
        if (getter == null) {
            return null;
        }
        boolean forever = ttlSeconds <= 0L;
        T result;
        //有锁则加锁
        if (StrUtil.isNotBlank(lockKey)) {
            //防止缓存击穿 即key突然失效,使得同一时间只有一条线程能访问数据库
            RLock lock = redissonClient.getLock(lockKey);
            try {
                lock.lock();
                logger.info("缓存: [{}]不存在, 调用getter", redisKey);
                result = getter.get();
                //将数据存入redis
                if (forever) {
                    redisTemplate.opsForValue().set(redisKey, result);
                } else {
                    redisTemplate.opsForValue().set(redisKey, result, ttlSeconds, TimeUnit.SECONDS);
                }
            } finally {
                //释放锁
                lock.unlock();
            }
        } else {
            result = getter.get();
            if (forever) {
                redisTemplate.opsForValue().set(redisKey, result);
            } else {
                redisTemplate.opsForValue().set(redisKey, result, ttlSeconds, TimeUnit.SECONDS);
            }
        }
        return result;
    }

    /**
     * 如果不存在则设置缓存
     *
     * @param name     缓存名称
     * @param key      缓存二级key
     * @param ttl      过期时间
     * @param timeUnit 时间单位
     * @param getter   获取数据函数
     * @param <T>      数据类型
     * @return 是否设置成功
     */
    public <T> boolean setNx(String name, String key, long ttl, TimeUnit timeUnit, RealDataGetter<T> getter) {
        String redisKey = buildKey(name, key);
        if (StrUtil.isEmpty(redisKey)) {
            return false;
        }
        Boolean result = redisTemplate.opsForValue().setIfAbsent(redisKey, getter.get(), ttl, timeUnit);
        return BooleanUtil.isTrue(result);
    }

    /**
     * 设置缓存
     *
     * @param name    缓存名称 用于取缓存过期时间
     * @param key     缓存名称后跟随的唯一表示 ,如 SYSTEM_USER_TREE:test 的test即为key
     * @param getter  数据获取
     * @param lockKey 锁标识
     * @return 需要的数据
     */
    public <T> T set(String name, String key, RealDataGetter<T> getter, String lockKey) {
        String redisKey = buildKey(name, key);
        long ttlSeconds = getCacheExpireSeconds(name);
        return set(redisKey, ttlSeconds, getter, lockKey);
    }

    /**
     * 设置缓存
     *
     * @param name       缓存名称 用于取缓存过期时间
     * @param key        缓存名称后跟随的唯一表示
     * @param ttlSeconds 过期时间单位秒
     * @param getter     数据获取
     * @param lockKey    锁键值
     * @return 需要的数据
     */
    public <T> T set(String name, String key, long ttlSeconds, RealDataGetter<T> getter, String lockKey) {
        String redisKey = buildKey(name, key);
        return set(redisKey, ttlSeconds, getter, lockKey);
    }

    /**
     * 获取缓存
     *
     * @param name 缓存名称 用于取缓存过期时间
     * @param key  缓存名称后跟随的唯一表示 ,如 SYSTEM_USER_TREE:test 的test即为key
     * @return 缓存
     */
    public <T> T get(String name, String key) {
        String redisKey = buildKey(name, key);
        return get(redisKey);
    }

    /**
     * 获取缓存
     *
     * @param redisKey 缓存key
     * @return 缓存数据
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String redisKey) {
        return (T) redisTemplate.opsForValue().get(redisKey);
    }


    /**
     * 删除缓存
     *
     * @param redisKeys 缓存key
     * @return 成功失败
     */
    public Long del(String... redisKeys) {
        return redisTemplate.delete(Arrays.asList(redisKeys));
    }

    /**
     * 删除缓存
     *
     * @param name 缓存名称
     * @param key  缓存名称后跟随的唯一表示 ,如 SYSTEM_USER_TREE:test 的test即为key
     * @return 成功失败
     */
    public Long del(String name, String key) {
        String redisKey = buildKey(name, key);
        return del(redisKey);
    }

    /**
     * 批量删除指定缓存名称的所有key
     *
     * @param name 缓存名称
     */
    @SuppressWarnings("unchecked")
    public void batchDel(String name) {
        redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations redisOperations) throws DataAccessException {
                String keyPattern = buildKey(name, "*");
                // 开启事务使得后续操作 原子性
                redisOperations.multi();
                Set<String> keys = redisTemplate.keys(keyPattern);
                if (CollectionUtil.isNotEmpty(keys)) {
                    redisTemplate.delete(keys);
                }
                return redisOperations.exec();
            }
        });
    }

    /**
     * redis 缓存环绕增强,先从缓存拿,拿不到则调用实际方法获取数据并缓存
     *
     * @param redisKey   缓存key
     * @param ttlSeconds 过期时间 单位秒
     * @param getter     数据获取
     * @param <T>        类型
     * @param lockKey    锁键值,为空则在设置缓存时不加锁
     * @return T
     */
    @SuppressWarnings("unchecked")
    public <T> T round(String redisKey, long ttlSeconds, RealDataGetter<T> getter, String lockKey) {
        //从缓存中提取数据
        T result = get(redisKey);
        if (result != null) {
            return result;
        } else {
            // 从getter中获取并设置到缓存
            return set(redisKey, ttlSeconds, getter, lockKey);
        }
    }

    /**
     * redis 缓存环绕增强
     *
     * @param redisKey     缓存key
     * @param ttlSeconds   过期时间 单位秒
     * @param getter       数据获取
     * @param resultCanUse 判断result是否有效
     * @param <T>          类型
     * @param lockKey      锁键值,为空则在设置缓存时不加锁
     * @return T
     */
    @SuppressWarnings("unchecked")
    public <T> T round(String redisKey, long ttlSeconds, Predicate<T> resultCanUse, RealDataGetter<T> getter, String lockKey) {
        //从缓存中提取数据
        T result = get(redisKey);
        if (result != null) {
            if (resultCanUse != null) {
                if (resultCanUse.test(result)) {
                    return result;
                }
                return set(redisKey, ttlSeconds, getter, lockKey);
            }
            return result;
        } else {
            // 从getter中获取并设置到缓存
            return set(redisKey, ttlSeconds, getter, lockKey);
        }
    }

    /**
     * redis 缓存环绕增强
     *
     * @param redisKey 缓存key
     * @param getter   数据获取
     * @param lockKey  锁键值
     * @return 需要的数据
     */
    public <T> T round(String redisKey, RealDataGetter<T> getter, String lockKey) {
        return round(redisKey, getCacheExpireSeconds(redisKey), getter, lockKey);
    }

    /**
     * 获取缓存,处理完成后移除缓存
     *
     * @param redisKey redis key
     * @param handler  数据处理
     * @return 缓存处理后的数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getAndDel(String redisKey, CacheHandler<T> handler) {
        T result = get(redisKey);
        if (handler != null) {
            handler.handle(result);
        }
        if (result != null) {
            redisTemplate.delete(redisKey);
        }
        return result;
    }

    /**
     * 获取缓存,处理完成后移除缓存
     *
     * @param redisKey redis key
     * @param function 数据处理
     * @return 缓存处理后的数据
     */
    @SuppressWarnings("unchecked")
    public <T, R> R getAndDelWithBack(String redisKey, Function<T, R> function) {
        T result = get(redisKey);
        R r = null;
        if (function != null) {
            r = function.apply(result);
        }
        if (result != null) {
            redisTemplate.delete(redisKey);
        }
        return r;
    }

    /**
     * 获取缓存,处理完成后移除缓存
     *
     * @param name    缓存名称 用于取缓存过期时间
     * @param key     缓存名称后跟随的唯一表示 ,如 SYSTEM_USER_TREE::test 的test即为key
     * @param handler 缓存处理
     * @return 处理后的缓存数据
     */
    public <T> T getAndDel(String name, String key, CacheHandler<T> handler) {
        String redisKey = buildKey(name, key);
        return getAndDel(redisKey, handler);
    }

    /**
     * 设置hash缓存
     *
     * @param hash    哈希key
     * @param hk      缓存key
     * @param getter  数据获取
     * @param lockKey 锁键值 使得同一时间只有一条线程去设置缓存,防止缓存击穿
     * @return 需要的数据
     */
    public <T> T hSet(String hash, String hk, RealDataGetter<T> getter, String lockKey) {
        T result = null;
        if (getter != null) {
            //有锁则加锁
            if (StrUtil.isNotBlank(lockKey)) {
                //防止缓存击穿 即key突然失效,使得同一时间只有一条线程能访问数据库
                RLock lock = redissonClient.getLock(lockKey);
                try {
                    lock.lock();
                    logger.info("缓存: [{}]不存在, 调用getter", hk);
                    result = getter.get();
                    if (result != null) {
                        //将数据存入redis
                        redisTemplate.opsForHash().put(hash, hk, result);
                    }
                    return result;
                } finally {
                    //释放锁
                    lock.unlock();
                }
            } else {
                result = getter.get();
                if (result != null) {
                    //将数据存入redis
                    redisTemplate.opsForHash().put(hash, hk, result);
                }
            }
        }
        return result;
    }

    /**
     * redis 缓存环绕增强
     *
     * @param redisKey 缓存key
     * @param hashKey  hashKey
     * @param getter   数据获取
     * @param lockKey  锁键值 使得同一时间只有一条线程去设置缓存,防止缓存击穿
     * @return 需要的数据
     */
    @SuppressWarnings("unchecked")
    public <T> T hashRound(String hashKey, String redisKey, RealDataGetter<T> getter, String lockKey) {
        //从缓存中提取数据
        T result = (T) redisTemplate.opsForHash().get(hashKey, redisKey);
        if (result != null) {
            return result;
        } else {
            return hSet(hashKey, redisKey, getter, lockKey);
        }
    }

    /**
     * 是否存在指定缓存
     *
     * @param name 缓存名称 用于取缓存过期时间
     * @param key  缓存名称后跟随的唯一表示 ,如 SYSTEM_USER_TREE:test 的test即为key
     * @return boolean 是否存在
     */
    public boolean exists(String name, String key) {
        String redisKey = buildKey(name, key);
        return exists(redisKey);
    }

    /**
     * 是否存在指定缓存
     *
     * @param redisKey 缓存key
     * @return boolean 是否存在
     */
    public boolean exists(String redisKey) {
        Boolean has = redisTemplate.hasKey(redisKey);
        return has != null && has;
    }

    /**
     * 获取指定key的过期时间(秒)
     *
     * @param name 缓存名称 用于取缓存过期时间
     * @param key  缓存名称后跟随的唯一表示 ,如 SYSTEM_USER_TREEtest 的test即为key
     * @return 过期时间 单位s redis 2.8之后 若key不存在则返回 -2
     */
    public Long ttl(String name, String key) {
        String redisKey = buildKey(name, key);
        return redisTemplate.getExpire(redisKey);
    }

    /**
     * 获取指定key的过期时间(秒)
     *
     * @param redisKey 缓存key
     * @return 过期时间 单位s redis 2.8之后 若key不存在则返回 -2
     */
    public Long ttl(String redisKey) {
        return redisTemplate.getExpire(redisKey);
    }

    /**
     * 获取指定key的过期时间(毫秒)
     *
     * @param redisKey key
     * @param timeUnit 时间单位
     * @return 过期时间  redis 2.8之后 若key不存在则返回 -2
     */
    public Long pttl(String redisKey, TimeUnit timeUnit) {
        return redisTemplate.getExpire(redisKey, timeUnit);
    }

    /**
     * 获取缓存
     *
     * @param hash hash
     * @param hk   hk
     * @return 缓存数据
     */
    @SuppressWarnings("unchecked")
    public <T> T hGet(String hash, String hk) {
        return (T) redisTemplate.opsForHash().get(hash, hk);
    }

    /**
     * 删除缓存
     *
     * @param hashKey   h
     * @param redisKeys 域
     * @return 成功失败
     */
    public Long hDel(String hashKey, Object... redisKeys) {
        if (redisKeys != null && redisKeys.length > 0) {
            return redisTemplate.opsForHash().delete(hashKey,
                    Arrays.stream(redisKeys)
                            .filter(o -> ObjectUtil.isNotNull(o) && StrUtil.isNotEmpty(o.toString()))
                            .map(Object::toString).toArray()
            );
        }
        return 0L;
    }

    /**
     * 指定hash key是否存在
     *
     * @param hashKey hash名称
     * @param key     key值
     * @return 是否存在
     */
    public boolean hExists(String hashKey, Object key) {
        return redisTemplate.opsForHash().hasKey(hashKey, key);
    }

    /**
     * hash key对应值不存在,则调用实际方法获取值并存入
     *
     * @param hashKey hash key
     * @param key     key
     * @param getter  获取数据函数
     * @param <T>     类型
     */
    public <T> void hashNxThenPut(String hashKey, Object key, RealDataGetter<T> getter) {
        if (!hExists(hashKey, key) && getter != null) {
            redisTemplate.opsForHash().put(hashKey, key, getter.get());
        }
    }

    /**
     * 获取缓存,处理完成后移除缓存
     *
     * @param hash    hash
     * @param hk      hk
     * @param handler 缓存处理
     * @return 处理后的缓存数据
     */
    public <T> T hGetAndDel(String hash, String hk, CacheHandler<T> handler) {
        T result = hGet(hash, hk);
        if (handler != null) {
            handler.handle(result);
        }
        hDel(hash, hk);
        return result;
    }


    /**
     * 设置key过期时间
     *
     * @param name 缓存名称
     * @param key  缓存名称后跟随的唯一表示 ,如 SYSTEM_USER_TREE:test 的test即为key
     * @param ttl  过期时间
     * @param unit 时间单位
     */
    public void expire(String name, String key, long ttl, TimeUnit unit) {
        String redisKey = buildKey(name, key);
        redisTemplate.expire(redisKey, ttl, unit);
    }

    /**
     * 设置key过期时间
     *
     * @param redisKey 缓存key
     * @param ttl  过期时间
     * @param unit 时间单位
     */
    public void expire(String redisKey, long ttl, TimeUnit unit) {
        redisTemplate.expire(redisKey, ttl, unit);
    }

    /**
     * 从list获取数据,从右边移除一个元素
     *
     * @param redisKey list key
     * @return T
     */
    @SuppressWarnings("unchecked")
    public <T> T rightPop(String redisKey) {
        return (T) redisTemplate.opsForList().rightPop(redisKey);
    }

    /**
     * push数据到list, 从左边添加一个元素
     *
     * @param redisKey list key
     * @param value    push的数据
     * @return push数据行
     */
    public <T> Long leftPush(String redisKey, T value) {
        return redisTemplate.opsForList().leftPush(redisKey, value);
    }

    /**
     * push数据到list
     *
     * @param redisKey list key
     * @param value    push的数据
     * @return push数据行
     */
    public <T> Long leftPushAll(String redisKey, T... value) {
        return redisTemplate.opsForList().leftPushAll(redisKey, value);
    }

    /**
     * 获取指定 List里元素个数
     *
     * @param redisKey list key
     * @return List里元素个数
     */
    public Long lLen(String redisKey) {
        return redisTemplate.opsForList().size(redisKey);
    }

    /**
     * 将name 过期时间配置存入本地
     *
     * @param name cache name
     * @param ttl  过期时间
     */
    public void put(String name, Duration ttl) {
        long seconds = ttl.getSeconds();
        if (seconds <= 0) {
            seconds = -1;
        }
        CONFIG_MAP.put(name, new CustomCacheConfig(name, seconds));
    }

    public void put(String name) {
        CONFIG_MAP.put(name, new CustomCacheConfig(name, -1L));
    }

    /**
     * 获取指定 cache name 或 key 配置的过期时间
     *
     * @param nameOrKey cache name 或 key
     * @return 过期时间 单位秒
     */
    public long getCacheExpireSeconds(String nameOrKey) {
        CustomCacheConfig config = CONFIG_MAP.get(nameOrKey);
        return config != null ? config.getSeconds() : DEFAULT_EXPIRE_SECONDS;
    }

    /**
     * 生成锁的key值
     *
     * @param nameOrKey 缓存名称或key
     * @return 锁的hash值
     */
    public RLock getLock(String nameOrKey) {
        return redissonClient.getLock(nameOrKey + REDIS_LOCK_SUFFIX);
    }

    /**
     * 锁并执行
     *
     * @param nameOrKey        锁名称
     * @param haveLockFun      获取到锁执行的函数
     * @param doNotHaveLockFun 未获取到锁时执行的函数
     */
    public void lockProcess(String nameOrKey, VoidFunc0 haveLockFun, VoidFunc0 doNotHaveLockFun) {
        RLock rLock = redissonClient.getLock(nameOrKey + REDIS_LOCK_SUFFIX);
        boolean hadLock = false;
        try {
            //尝试获取锁 30s自动释放
            hadLock = rLock.tryLock();
            if (!hadLock) {
                doNotHaveLockFun.callWithRuntimeException();
            } else {
                haveLockFun.callWithRuntimeException();
            }
        } finally {
            if (hadLock) {
                rLock.unlock();
            }
        }
    }

    /**
     * 锁并执行
     *
     * @param nameOrKey        锁名称
     * @param haveLockFun      获取到锁执行的函数
     * @param doNotHaveLockFun 未获取到锁时执行的函数
     */
    public <R> R lockProcess(String nameOrKey, Func0<R> haveLockFun, VoidFunc0 doNotHaveLockFun) {
        RLock rLock = redissonClient.getLock(nameOrKey + REDIS_LOCK_SUFFIX);
        boolean hadLock = false;
        try {
            //尝试获取锁 30s自动释放
            hadLock = rLock.tryLock();
            if (!hadLock) {
                doNotHaveLockFun.callWithRuntimeException();
            } else {
                return haveLockFun.callWithRuntimeException();
            }
        } finally {
            if (hadLock) {
                rLock.unlock();
            }
        }
        return null;
    }

    /**
     * 联锁并执行
     *
     * @param lockKeyPrefix    锁前缀
     * @param lockKeys         锁名称
     * @param haveLockFun      获取到锁执行的函数
     * @param doNotHaveLockFun 未获取到锁时执行的函数
     */
    public void mulLockProcess(String lockKeyPrefix, Collection<String> lockKeys, VoidFunc0 haveLockFun, VoidFunc0 doNotHaveLockFun) {
        RedissonMultiLock lock = new RedissonMultiLock(lockKeys.stream()
                .map(key -> redissonClient.getLock(buildKey(lockKeyPrefix, key, REDIS_LOCK_SUFFIX)))
                .toArray(RLock[]::new)
        );
        boolean hadLock = false;
        try {
            //尝试获取锁 30s自动释放
            hadLock = lock.tryLock();
            if (!hadLock) {
                doNotHaveLockFun.callWithRuntimeException();
            } else {
                haveLockFun.callWithRuntimeException();
            }
        } finally {
            if (hadLock) {
                lock.unlock();
            }
        }
    }

    /**
     * 联锁并执行
     *
     * @param lockKeyPrefix    锁前缀
     * @param lockKeys         锁名称
     * @param haveLockFun      获取到锁执行的函数
     * @param doNotHaveLockFun 未获取到锁时执行的函数
     */
    public <R> R mulLockProcess(String lockKeyPrefix, Collection<String> lockKeys, Func0<R> haveLockFun, VoidFunc0 doNotHaveLockFun) {
        RedissonMultiLock lock = new RedissonMultiLock(lockKeys.stream()
                .map(key -> redissonClient.getLock(buildKey(lockKeyPrefix, key, REDIS_LOCK_SUFFIX)))
                .toArray(RLock[]::new)
        );
        boolean hadLock = false;
        try {
            //尝试获取锁 30s自动释放
            hadLock = lock.tryLock();
            if (!hadLock) {
                doNotHaveLockFun.callWithRuntimeException();
            } else {
                return haveLockFun.callWithRuntimeException();
            }
        } finally {
            if (hadLock) {
                lock.unlock();
            }
        }
        return null;
    }

    /**
     * 使用redis底层命令,单个连接执行多个redis命令
     *
     * @param callback 执行
     * @param pipeline 管道执行,将多个命令的结果返回
     * @param <T>      返回类型
     * @return 返回
     */
    public <T> T execute(RedisCallback<T> callback, boolean pipeline) {
        return redisTemplate.execute(callback, true, pipeline);
    }

    /**
     * 使用spring封装的redis命令,更友好
     *
     * @param callback 执行
     * @param <T>      返回类型
     * @return 返回
     */
    public <T> T execute(SessionCallback<T> callback) {
        return redisTemplate.execute(callback);
    }

    /**
     * 序列化key
     *
     * @param redisKey key值
     * @return 结果
     */
    public byte[] serializeKey(String redisKey) {
        return keySerializer.serialize(redisKey);
    }

    /**
     * 序列化key
     *
     * @param name 缓存名称
     * @param key  缓存key值
     * @return 结果
     */
    public byte[] serializeKey(String name, String key) {
        return keySerializer.serialize(buildKey(name, key));
    }

    /**
     * 序列化数据
     *
     * @param value 数据
     * @return 结果
     */
    public <T> byte[] serializeValue(T value) {
        return valueSerializer.serialize(value);
    }

    /**
     * 序列化数据
     *
     * @param getter 获取数据的函数
     * @return 结果
     */
    public <T> byte[] serializeValue(RealDataGetter<T> getter) {
        return valueSerializer.serialize(getter.get());
    }

    @SuppressWarnings("unchecked")
    public <T> T deserializeValue(byte[] bytes) {
        return (T) valueSerializer.deserialize(bytes);
    }

    /**
     * 执行Lua脚本
     *
     * @param lua        脚本
     * @param resultType lua脚本返回数据类型
     * @param keys       keys
     * @param args       参数
     * @param <T>        返回类型
     * @return 返回
     */
    public <T> T executeLua(String lua, Class<T> resultType, List<String> keys, Object... args) {
        RedisScript<T> redisScript = new DefaultRedisScript<>(lua, resultType);
        return redisTemplate.execute(redisScript, keys, args);
    }

    /**
     * 批量获取hash 缓存值
     *
     * @param hashKey hash key
     * @param keys    key
     * @param <T>     响应类型
     * @return 结果集
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> hMGet(String hashKey, List keys) {
        return (List<T>) redisTemplate.opsForHash().multiGet(hashKey, keys);
    }

    /**
     * 批量设置hash 缓存值
     *
     * @param hashKey   hash key
     * @param keyValues 只
     */
    public void hMSet(String hashKey, Map<String, Object> keyValues) {
        redisTemplate.opsForHash().putAll(hashKey, keyValues);
    }

    /**
     * 自增序列号 形如 DC20240113 0001
     *
     * @param prefix   号码前缀
     * @param len      长度,不够前补0
     * @param duration 过期时间
     * @return 号码
     */
    public String incSn(String prefix, int len, Duration duration) {
        String redisKey = buildKey(AUTO_INC_SN, prefix);
        Long increment = executeLua(INCREMENT_AND_TTL_LUA, Long.class, CollUtil.newArrayList(redisKey), duration.toMillis() / 1000, 1);
        return StrBuilder.create(prefix, StrUtil.fillBefore(String.valueOf(increment), '0', len)).toString();
    }

    /**
     * redis 自增值
     *
     * @param name key前缀
     * @param key  key
     * @param step 步长
     * @return 前值
     */
    public Long increment(String name, String key, long step) {
        return redisTemplate.opsForValue().increment(buildKey(name, key), step);
    }

    /**
     * redis 自增并设置ttl
     *
     * @param name       key前缀
     * @param key        key
     * @param ttlSeconds ttl时间,单位秒
     * @param step       步长
     * @return 当前值
     */
    public Long incrementAndTtl(String name, String key, Integer ttlSeconds, Integer step) {
        String redisKey = buildKey(name, key);
        List<String> keys = CollUtil.newArrayList(redisKey);
        return executeLua(INCREMENT_AND_TTL_LUA, Long.class, keys, ttlSeconds, step);
    }

    public RedisTemplate<String, Object> template() {
        return redisTemplate;
    }

    /**
     * @author yq
     * @date 2020/10/15 17:51
     * @description 自定义缓存名称
     * @since V1.0.0
     */
    @Data
    @AllArgsConstructor
    public static class CustomCacheConfig {
        /**
         * cache name, 等同于@Cacheable cacheName
         */
        private String name;
        /**
         * 缓存过期时间 秒
         */
        private Long seconds;
    }

    /**
     * 实际数据获取
     *
     * @param <T> 获取的数据类型
     */
    public interface RealDataGetter<T extends Object> {
        /**
         * 调用接口或sql获取实际数据
         *
         * @return 实际数据
         */
        T get();
    }

    /**
     * 获取缓存后的处理
     */
    public interface CacheHandler<T extends Object> {
        /**
         * 处理数据
         *
         * @param t 缓存数据
         */
        void handle(T t);
    }
}
