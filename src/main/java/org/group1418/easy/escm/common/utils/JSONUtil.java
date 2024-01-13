package org.group1418.easy.escm.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author yq 2023/10/24 17:08
 * @description JsonUtil
 */
public class JSONUtil {


    public static final Pattern SQUARE_BRACKETS_NUMBER_PATTERN = Pattern.compile("\\[(\\d+)]");
    private static final String ARRAY_TOP_TEMP_KEY = "tempTopArray.";

    /**
     * 快速获取fastjson 中的对象, 多层嵌套获取
     *
     * @param jsonOrArray 原始json对象或数组
     * @param resultClazz 需要的结果对象类型
     * @param keyExpress  表达式,形如 json.array[0].c.key, 或[0].c.key
     * @param <T>         响应结果类型
     * @return 响应结果
     */
    public static <T> T getObject(JSONAware jsonOrArray, Class<T> resultClazz, String keyExpress) {
        if (jsonOrArray instanceof JSONObject) {
            return getObjectFromJson((JSONObject) jsonOrArray, resultClazz, keyExpress);
        } else if (jsonOrArray instanceof JSONArray) {
            //集合就在外面包装一层
            JSONObject json = new JSONObject().fluentPut(ARRAY_TOP_TEMP_KEY, jsonOrArray);
            return getObjectFromJson(json, resultClazz, ARRAY_TOP_TEMP_KEY + keyExpress);
        }
        return null;
    }

    /**
     * 快速获取fastjson 中的对象, 多层嵌套获取
     *
     * @param json        原始json对象
     * @param resultClazz 需要的结果对象类型
     * @param keyExpress  表达式,形如 json.array[0].c.key
     * @param <T>         响应结果类型
     * @return 响应结果
     */
    public static <T> T getObjectFromJson(JSONObject json, Class<T> resultClazz, String keyExpress) {
        if (StrUtil.isBlank(keyExpress)) {
            return null;
        }
        if (MapUtil.isEmpty(json)) {
            return null;
        }
        if (resultClazz == null) {
            return null;
        }
        //使用 .拆分
        String[] keys = keyExpress.split("\\.");
        JSONObject current = json;
        int length = keys.length;
        if (length == 1) {
            return json.getObject(keys[0], resultClazz);
        }
        JSONArray mayArray;
        //数组key,
        String arrayKey, arrayStr;
        int arrayIndex;
        for (int i = 0; i < length - 1; i++) {
            //形如a[0]
            arrayStr = ReUtil.getGroup1(SQUARE_BRACKETS_NUMBER_PATTERN, keys[i]);
            if (StrUtil.isNotBlank(arrayStr)) {
                arrayKey = keys[i].split("\\[")[0];
                mayArray = current.getJSONArray(arrayKey);
                if (CollUtil.isEmpty(mayArray)) {
                    return null;
                }
                arrayIndex = Integer.parseInt(arrayStr);
                if (arrayIndex >= 0 && arrayIndex < mayArray.size()) {
                    current = mayArray.getJSONObject(arrayIndex);
                } else {
                    return null;
                }
            } else {
                current = current.getJSONObject(keys[i]);
            }
            if (current == null) {
                return null;
            }
        }
        return current.getObject(keys[length - 1], resultClazz);
    }


    public static String getString(JSONAware json, String keyExpress) {
        return getObject(json, String.class, keyExpress);
    }

    public static Boolean notBlank(JSONAware json, String keyExpress){
        return StrUtil.isNotBlank(getString(json,keyExpress));
    }

    public static Boolean getBoolean(JSONAware json, String keyExpress) {
        return getObject(json, Boolean.class, keyExpress);
    }

    public static Long getLong(JSONAware json, String keyExpress) {
        return getObject(json, Long.class, keyExpress);
    }

    public static Integer getInteger(JSONAware json, String keyExpress) {
        return getObject(json, Integer.class, keyExpress);
    }

    public static Boolean inStr(JSONAware json, String keyExpress, String... array) {
        String object = getString(json, keyExpress);
        if (StrUtil.isBlank(object)) {
            return false;
        }
        return StrUtil.equalsAny(object, array);
    }

    /**
     * json数组对象拆分 分组
     *
     * @param array 数组对象
     * @param size  分组大小
     * @return 拆分后的集合
     */
    public static List<List<JSONObject>> splitJSONArray(JSONArray array, int size) {
        final List<List<JSONObject>> result = new ArrayList<>();
        if (CollUtil.isEmpty(array)) {
            return result;
        }

        ArrayList<JSONObject> subList = new ArrayList<>(size);
        for (int i = 0; i < array.size(); i++) {
            if (subList.size() >= size) {
                result.add(subList);
                subList = new ArrayList<>(size);
            }
            subList.add(array.getJSONObject(i));
        }
        result.add(subList);
        return result;
    }

    /**
     * 对象转JSON 过滤部分属性
     *
     * @param object             对象
     * @param ignoreProperties   不序列化的属性名称
     * @param serializerFeatures 序列化配置
     * @return json字符串
     */
    public static String toJSONStringWithIgnoreProperties(Object object, List<String> ignoreProperties, SerializerFeature... serializerFeatures) {
        return JSON.toJSONString(object,
                (PropertyFilter) (obj, name, value) ->
                        CollectionUtil.isEmpty(ignoreProperties) || !ignoreProperties.contains(name), serializerFeatures);
    }

    /**
     * 将json对象按key字典排序并转化为 key=value&key2=value2形式
     *
     * @param json json对象
     * @return 结果集
     */
    public static String json2PathParams(JSONObject json) {
        if (CollUtil.isNotEmpty(json)) {
            return json.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + json.getString(entry.getKey())).collect(Collectors.joining("&"));
        }
        return null;
    }

    /**
     * 转化json字符串为json 对象
     * @param jsonOrArrayStr 数组或对象json字符串
     * @return 响应
     */
    public static JSONAware parse(String jsonOrArrayStr){
        if(StrUtil.isBlank(jsonOrArrayStr)){
            return null;
        }
        String trim = jsonOrArrayStr.trim();
        return StrUtil.startWith(trim,"[") ? JSON.parseArray(trim) : JSON.parseObject(trim);
    }
}
