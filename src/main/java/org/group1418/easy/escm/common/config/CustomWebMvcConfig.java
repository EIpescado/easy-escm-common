package org.group1418.easy.escm.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.group1418.easy.escm.common.config.properties.CustomConfigProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yq
 * @date 2018/04/18 14:05
 * @description web上下文配置
 * @since V1.0.0
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurationSupport.class})
@Slf4j
public class CustomWebMvcConfig extends WebMvcConfigurationSupport {

    private final FastJsonHttpMessageConverter fastJsonHttpMessageConverter;
    private final CustomConfigProperties configProperties;

    public CustomWebMvcConfig(CustomConfigProperties configProperties) {
        this.fastJsonHttpMessageConverter = createDefaultFastJsonHttpMessageConverter();
        this.configProperties = configProperties;
    }

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("注入 FastJsonHttpMessageConverter");
        converters.add(fastJsonHttpMessageConverter);
        //直接返回图片 消息转化器
        converters.add(new BufferedImageHttpMessageConverter());
    }

    @Bean
    @Primary
    public FastJsonHttpMessageConverter getDefaultFastJsonHttpMessageConverter() {
        return fastJsonHttpMessageConverter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能,除开 @SaIgnore 标识和 配置所有接口需登录,
        registry.addInterceptor(new SaInterceptor(handle -> SaRouter
                .match("/**")
                //忽略校验登录的path
                .notMatch(configProperties.getTokenConfig().getNotCheckLoginPaths())
                .check(r -> StpUtil.checkLogin()))).addPathPatterns("/**");
    }

    private FastJsonHttpMessageConverter createDefaultFastJsonHttpMessageConverter() {
        log.info("createDefaultFastJsonHttpMessageConverter ");
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        //不可使用*/*,强制用户自己定义支持的MediaTypes
        fastConverter.setSupportedMediaTypes(new ArrayList<MediaType>() {
            private static final long serialVersionUID = 2644645137309978808L;

            {
                add(MediaType.APPLICATION_JSON);
                add(MediaType.TEXT_PLAIN);
            }
        });
        //全局配置
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(
                //是否输出值为null的字段,默认false
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty,
                //全局修改日期格式,默认false,格式为 yyyy-MM-dd
                SerializerFeature.WriteDateUseDateFormat,
                //字符串输出null值
                SerializerFeature.WriteNullStringAsEmpty
        );
        //整型转字符串 防止前端精度丢失
        SerializeConfig serializeConfig = SerializeConfig.globalInstance;
        serializeConfig.put(Long.class, ToStringSerializer.instance);
        serializeConfig.put(Long.TYPE, ToStringSerializer.instance);
        fastJsonConfig.setSerializeConfig(serializeConfig);
        //全局日期格式
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        fastConverter.setFastJsonConfig(fastJsonConfig);
        return fastConverter;
    }
}
