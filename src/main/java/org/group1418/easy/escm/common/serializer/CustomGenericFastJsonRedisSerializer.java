package org.group1418.easy.escm.common.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.IOUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * @author yq
 * @date 2021/04/16 23:08
 * @description
 * @since V1.0.0 参照 GenericFastJsonRedisSerializer 解决autoType 问题
 */
public class CustomGenericFastJsonRedisSerializer implements RedisSerializer<Object> {

    private static final ParserConfig PARSER_CONFIG = ParserConfig.getGlobalInstance();

    static {
        PARSER_CONFIG.setAutoTypeSupport(true);
    }

    public void addAccept(String... packagePrefix) {
        //添加autotype白名单
        if (packagePrefix != null && packagePrefix.length > 0) {
            Arrays.stream(packagePrefix).forEach(PARSER_CONFIG::addAccept);
        }
    }

    public void putDeserializer(Type type, ObjectDeserializer deserializer) {
        PARSER_CONFIG.putDeserializer(type, deserializer);
    }

    public void setAutoTypeSupport(boolean autoTypeSupport) {
        PARSER_CONFIG.setAutoTypeSupport(autoTypeSupport);
    }

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return new byte[0];
        } else {
            try {
                return JSON.toJSONBytes(object, SerializerFeature.WriteClassName);
            } catch (Exception var3) {
                throw new SerializationException("Could not serialize: " + var3.getMessage(), var3);
            }
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes != null && bytes.length != 0) {
            try {
                return JSON.parseObject(new String(bytes, IOUtils.UTF8), Object.class, PARSER_CONFIG);
            } catch (Exception var3) {
                throw new SerializationException("Could not deserialize: " + var3.getMessage(), var3);
            }
        } else {
            return null;
        }
    }
}
