package org.group1418.easy.escm.common.saToken;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.group1418.easy.escm.common.config.properties.CustomConfigProperties;
import org.group1418.easy.escm.common.wrapper.R;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yq 2024/2/21 15:17
 * @description SaTokenConfig
 */
@Configuration
@Slf4j
public class SaTokenConfig {

    @Bean
    @Primary
    public cn.dev33.satoken.config.SaTokenConfig getSaTokenConfigPrimary(CustomConfigProperties customConfigProperties) {
        return customConfigProperties.getTokenConfig();
    }

    public static R<String> saTokenExceptionHandler(SaTokenException e) {
        if (e instanceof NotLoginException) {
            NotLoginException nle = (NotLoginException) e;
            String type = nle.getType();
            Map<String, String> notLoginErrorMap = new HashMap<>(8);
            notLoginErrorMap.put(NotLoginException.NOT_TOKEN, NotLoginException.NOT_TOKEN_MESSAGE);
            notLoginErrorMap.put(NotLoginException.INVALID_TOKEN, NotLoginException.INVALID_TOKEN_MESSAGE);
            notLoginErrorMap.put(NotLoginException.TOKEN_TIMEOUT, NotLoginException.TOKEN_TIMEOUT_MESSAGE);

            notLoginErrorMap.put(NotLoginException.BE_REPLACED, NotLoginException.BE_REPLACED_MESSAGE);
            notLoginErrorMap.put(NotLoginException.KICK_OUT, NotLoginException.KICK_OUT_MESSAGE);
            notLoginErrorMap.put(NotLoginException.TOKEN_FREEZE, NotLoginException.TOKEN_FREEZE_MESSAGE);
            notLoginErrorMap.put(NotLoginException.NO_PREFIX, NotLoginException.NO_PREFIX_MESSAGE);
            String message = notLoginErrorMap.get(type);
            if (StrUtil.isBlank(message)) {
                message = "当前会话未登录";
            }
            return R.fail(e.getCode(), message);
        }
        return R.fail(e.getCode(), e.getMessage());
    }

}
