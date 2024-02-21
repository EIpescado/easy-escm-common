package org.group1418.easy.escm.common.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group1418.easy.escm.common.config.properties.CustomConfigProperties;
import org.group1418.easy.escm.common.exception.CustomException;
import org.group1418.easy.escm.common.spring.SpringContextHolder;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author yq
 * @date 2020/11/26 16:45
 * @description spring 异步线程池设置,使用默认可能导致内存溢出,默认实现为 SimpleAsyncTaskExecutor,通过 AsyncExecutionInterceptor 来选择执行器
 * @since V1.0.0
 */
@ConditionalOnClass(ThreadPoolTaskExecutor.class)
@Configuration
@Slf4j
@EnableAsync
@RequiredArgsConstructor
public class CustomAsyncTaskConfig implements AsyncConfigurer, InitializingBean {

    private final CustomConfigProperties configProperties;
    public static final String TASK_EXECUTOR_SUFFIX = "-TaskExecutor";


    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            String message;
            if (throwable instanceof CustomException) {
                CustomException customException = (CustomException) throwable;
                message = customException.getTip().getMsg();
                log.info("[{}]异步执行异常: [{}]", method.getName(), message);
            } else {
                message = throwable.getLocalizedMessage();
                log.info("[{}]异步执行异常: [{}]", method.getName(), message, throwable);
            }
        };
    }

    /**
     * 默认TaskExecutor,覆盖boot自动注入的默认
     */
    @Bean(name = {TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME, AsyncAnnotationBeanPostProcessor.DEFAULT_TASK_EXECUTOR_BEAN_NAME})
    @Primary
    public ThreadPoolTaskExecutor applicationTaskExecutor() {
        CustomConfigProperties.AsyncConfig asyncExecutorConfigEntry = new CustomConfigProperties.AsyncConfig();
        String taskExecutor = configProperties.getName() + TASK_EXECUTOR_SUFFIX;
        log.info("注入[{}]", taskExecutor);
        return this.createExecutor(taskExecutor, asyncExecutorConfigEntry);
    }


    /***
     * 创建线程执行器, 多个时需指定 @Async(ExecutorNameConstant.LOG)
     * @param prefix 线程前缀
     * @param asyncExecutorConfigEntry 线程池配置
     * @return Executor
     */
    private ThreadPoolTaskExecutor createExecutor(String prefix, CustomConfigProperties.AsyncConfig asyncExecutorConfigEntry) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(asyncExecutorConfigEntry.getCoreSize());
        taskExecutor.setMaxPoolSize(asyncExecutorConfigEntry.getMaxSize());
        taskExecutor.setQueueCapacity(asyncExecutorConfigEntry.getQueueCapacity());
        taskExecutor.setKeepAliveSeconds(asyncExecutorConfigEntry.getKeepAliveSeconds());
        taskExecutor.setAllowCoreThreadTimeOut(asyncExecutorConfigEntry.isAllowCoreThreadTimeout());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(asyncExecutorConfigEntry.isWaitForTasksToCompleteOnShutdown());
        taskExecutor.setAwaitTerminationSeconds(asyncExecutorConfigEntry.getAwaitTerminationSeconds());
        //线程名称前缀
        taskExecutor.setThreadNamePrefix(prefix + StrUtil.DASHED);
        /**
         * 拒绝策略，默认是AbortPolicy
         * AbortPolicy：丢弃任务并抛出RejectedExecutionException异常
         * DiscardPolicy：丢弃任务但不抛出异常
         * DiscardOldestPolicy：丢弃最旧的处理程序，然后重试，如果执行器关闭，这时丢弃任务
         * CallerRunsPolicy：执行器执行任务失败，则在策略回调方法中执行任务，如果执行器关闭，这时丢弃任务
         */
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return taskExecutor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (configProperties != null && CollUtil.isNotEmpty(configProperties.getAsyncConfigs())) {
            configProperties.getAsyncConfigs().forEach(c -> {
                String threadTaskExecutor = c.getThreadName() + TASK_EXECUTOR_SUFFIX;
                if (StrUtil.isNotBlank(threadTaskExecutor)) {
                    SpringContextHolder.putWaitImportBeans(threadTaskExecutor, this.createExecutor(threadTaskExecutor, c));
                }
            });
        }
    }
}
