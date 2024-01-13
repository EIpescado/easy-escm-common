package org.group1418.easy.escm.common.serializer;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * @author yq
 * @date 2023年9月1日 11:33:07
 * @description 数字转百分数
 * @since V1.0.0
 */
public class PercentNumberSerializer implements ObjectSerializer {

    public static final BigDecimal HUNDRED = new BigDecimal("100");

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type type, int i) throws IOException {
        SerializeWriter out = serializer.out;
        Number value = (Number) object;
        if (value == null) {
            out.writeNull();
        } else {
            out.writeString(NumberUtil.round(NumberUtil.div(value, HUNDRED), 2).toString());
        }
    }
}
