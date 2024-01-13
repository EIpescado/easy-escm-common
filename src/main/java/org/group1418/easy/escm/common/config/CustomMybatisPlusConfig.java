package org.group1418.easy.escm.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.group1418.easy.escm.common.config.properties.CustomConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;


/**
 * @author yq
 * @date 2021年4月14日 11:25:49
 * @description mybatis-plus config
 * @since V1.0.0
 */
@Configuration
@Slf4j
@AutoConfigureAfter({MybatisPlusAutoConfiguration.class})
public class CustomMybatisPlusConfig {


    public CustomMybatisPlusConfig() {
        log.info("注入 mybatisPlusInterceptor, metaObjectHandler");
    }

    /**
     * mybatis-plus分页插件
     */
    @Bean
    @ConditionalOnClass(MybatisPlusInterceptor.class)
    public MybatisPlusInterceptor mybatisPlusInterceptor(CustomConfigProperties plusConfigProperties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        String dbType = plusConfigProperties.getDbType();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.getDbType(dbType)));
        return interceptor;
    }

    /**
     * 自动填充值
     */
    @Bean
    @ConditionalOnClass(MetaObjectHandler.class)
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            private static final String DATE_CREATED = "dateCreated";
            private static final String LAST_UPDATED = "lastUpdated";
            private static final String CREATOR_ID = "creatorId";
            private static final String CREATOR = "creator";
            private static final String MODIFIER_ID = "modifierId";
            private static final String MODIFIER = "modifier";

            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = null;
                this.setFieldValByName("enabled", true, metaObject);
                if (getFieldValByName(DATE_CREATED, metaObject) == null) {
                    now = LocalDateTime.now();
                    this.setFieldValByName(DATE_CREATED, now, metaObject);
                }
                if (getFieldValByName(LAST_UPDATED, metaObject) == null) {
                    this.setFieldValByName(LAST_UPDATED, now != null ? now : LocalDateTime.now(), metaObject);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.setFieldValByName(LAST_UPDATED, LocalDateTime.now(), metaObject);
            }
        };
    }
}
