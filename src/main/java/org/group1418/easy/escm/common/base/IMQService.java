package org.group1418.easy.escm.common.base;

import org.group1418.easy.escm.common.exception.SystemCustomException;
import org.group1418.easy.escm.common.wrapper.OpLogDto;

import java.util.concurrent.TimeUnit;

/**
 * @author yuqian MQ接口
 */
public interface IMQService {

    /**
     * 发送json消息
     * @param destination 目的地
     * @param object 对象
     */
    void sendJsonMessage(String destination, Object object);

    /**
     * 发送系统日志消息
     * @param object 日志对象
     * @throws SystemCustomException 发送异常
     */
    void sendOpLogMessage(OpLogDto object);

    /**
     * 发送延迟json消息
     * @param destination 目的地
     * @param object 对象
     * @param time 延迟时间
     * @param timeUnit 单位
     */
    void sendDelayedJsonMessage(String destination, Object object, long time, TimeUnit timeUnit);
}
