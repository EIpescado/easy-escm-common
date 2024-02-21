package org.group1418.easy.escm.common.saToken;

import cn.dev33.satoken.json.SaJsonTemplate;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author yq 2024/2/20 17:59
 * @description SaJsonTemplateForFastJson 覆盖默认注入的SaJsonTemplateForJackson
 */
@Component
@Primary
public class SaJsonTemplateForFastJson implements SaJsonTemplate {

    @Override
    public String toJsonString(Object obj) {
        return obj != null ? JSON.toJSONString(obj) : null;
    }

    @Override
    public Map<String, Object> parseJsonToMap(String jsonStr) {
        return StrUtil.isNotBlank(jsonStr) ? JSON.parseObject(jsonStr) : MapUtil.empty();
    }
}
