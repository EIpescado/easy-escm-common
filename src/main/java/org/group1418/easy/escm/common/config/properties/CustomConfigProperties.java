package org.group1418.easy.escm.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * @author yq 2021/5/21 20:02
 * @description CustomMybatisPlusConfigProperties
 */
@ConfigurationProperties(prefix = "easy.escm")
@Data
@Component
public class CustomConfigProperties {

    /**
     * 日志文件路径
     */
    private String logPath;
    /**
     * 日志打印级别
     */
    private String logLevel;

    /**
     * 数据库类型, 多个用逗号相隔
     */
    private String dbType = "mysql";

    /**
     * 新增用户的默认密码
     */
    private String userDefaultPassword = "123456";

    /**
     * 异步线程池配置
     */
    private Map<String, AsyncConfig> asyncConfigs;


    @Data
    public static class AsyncConfig {
        /**
         * 核心线程数
         */
        private Integer coreSize = 2;
        /**
         * 线程池最大线程数
         */
        private Integer maxSize = 8;
        /**
         * 线程队列最大线程数
         */
        private Integer queueCapacity = 4096;
        /**
         * 线程池中线程最大空闲时间，默认：60，单位：秒
         */
        private Integer keepAliveSeconds = 60;
        /**
         * 核心线程是否允许超时，默认false
         */
        private boolean allowCoreThreadTimeout = true;
        /**
         * IOC容器关闭时是否阻塞等待剩余的任务执行完成，默认:false（必须设置setAwaitTerminationSeconds）
         */
        private boolean waitForTasksToCompleteOnShutdown = false;
        /**
         * 阻塞IOC容器关闭的时间，默认：10秒（必须设置setWaitForTasksToCompleteOnShutdown）
         */
        private int awaitTerminationSeconds = 10;
    }
}
