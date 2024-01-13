package org.group1418.easy.escm.common.deserializer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import org.group1418.easy.escm.common.utils.PudgeUtil;

import java.lang.reflect.Type;

/**
 * @author yq 2021/10/13 9:26
 * @description StringFull2HalfDeserializer 字符全角转半角移除前后空格
 */
public class StrFull2HalfTrimDeserializer implements ObjectDeserializer {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        final String input = parser.lexer.stringVal();
        if(StrUtil.isNotBlank(input)){
            return (T) PudgeUtil.full2Half(input.trim());
        }
        return null;
    }

    @Override
    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }
}
