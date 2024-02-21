package org.group1418.easy.escm.common.saToken;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group1418.easy.escm.common.service.CustomRedisCacheService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author yq 2024/2/20 10:49
 * @description SaTokenDaoRedis sa-token redis实现
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SaTokenRedisDao implements SaTokenDao {

    private final CustomRedisCacheService customRedisCacheService;

    @Override
    public String get(String key) {
        return customRedisCacheService.get(key);
    }

    @Override
    public void set(String key, String value, long timeout) {
        if (timeout != 0L && timeout > -2L) {
            if (timeout == -1L) {
                this.customRedisCacheService.template().opsForValue().set(key, value);
            } else {
                this.customRedisCacheService.template().opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void update(String key, String value) {
        long expire = this.getTimeout(key);
        if (expire != -2L) {
            this.set(key, value, expire);
        }
    }

    @Override
    public void delete(String key) {
        customRedisCacheService.del(key);
    }

    @Override
    public long getTimeout(String key) {
        return customRedisCacheService.ttl(key);
    }

    @Override
    public void updateTimeout(String key, long timeout) {
        if (timeout == -1L) {
            long expire = this.getTimeout(key);
            if (expire != -1L) {
                this.set(key, this.get(key), timeout);
            }
        } else {
            customRedisCacheService.expire(key, timeout, TimeUnit.SECONDS);
        }
    }

    @Override
    public Object getObject(String key) {
        return customRedisCacheService.template().opsForValue().get(key);
    }

    @Override
    public void setObject(String key, Object object, long timeout) {
        if (timeout != 0L && timeout > -2L) {
            if (timeout == -1L) {
                this.customRedisCacheService.template().opsForValue().set(key, object);
            } else {
                this.customRedisCacheService.template().opsForValue().set(key, object, timeout, TimeUnit.SECONDS);
            }

        }
    }

    @Override
    public void updateObject(String key, Object object) {
        long expire = this.getObjectTimeout(key);
        if (expire != -2L) {
            this.setObject(key, object, expire);
        }
    }

    @Override
    public void deleteObject(String key) {
        customRedisCacheService.del(key);
    }

    @Override
    public long getObjectTimeout(String key) {
        return customRedisCacheService.ttl(key);
    }

    @Override
    public void updateObjectTimeout(String key, long timeout) {
        if (timeout == -1L) {
            long expire = this.getObjectTimeout(key);
            if (expire != -1L) {
                this.setObject(key, this.getObject(key), timeout);
            }
        } else {
            customRedisCacheService.expire(key, timeout, TimeUnit.SECONDS);
        }
    }

    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        Set<String> keys = customRedisCacheService.template().keys(prefix + "*" + keyword + "*");
        if (CollUtil.isEmpty(keys)) {
            return ListUtil.empty();
        }
        List<String> list = ListUtil.toList(keys);
        return SaFoxUtil.searchList(list, start, size, sortType);
    }
}
