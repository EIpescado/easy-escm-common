package org.group1418.easy.escm.common.deserializer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import org.group1418.easy.escm.common.exception.SystemCustomException;

import java.lang.reflect.Type;

/**
 * @author yq 2023年5月22日 13:38:41
 * @description EnumCodeDeserializer 枚举编码 非序列化为枚举
 */
public class EnumCodeDeserializer implements ObjectDeserializer {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        final int token = lexer.token();
        Class clazz = (Class) type;
        Object[] enumConstants = clazz.getEnumConstants();
        //没实现EnumValue接口的 默认的按名字或者按ordinal
        if (token == JSONToken.LITERAL_INT) {
            int intValue = lexer.intValue();
            lexer.nextToken(JSONToken.COMMA);

            if (intValue < 0 || intValue > enumConstants.length) {
                throw new SystemCustomException("parse enum " + clazz.getName() + " error, value : " + intValue);
            }
            return (T) enumConstants[intValue];
        } else if (token == JSONToken.LITERAL_STRING) {
            if (StrUtil.isBlank(lexer.stringVal())) {
                return null;
            }
            return (T) Enum.valueOf(clazz, lexer.stringVal());
        }
        return null;
    }

    @Override
    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }
}
