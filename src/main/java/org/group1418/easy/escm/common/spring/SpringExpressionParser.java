package org.group1418.easy.escm.common.spring;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSONObject;
import org.group1418.easy.escm.common.utils.JSONUtil;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yq 2023/10/24 13:48
 * @description SpringExpressionParser 自定义spring EL转化
 */
public class SpringExpressionParser {


    private static final Map<String, Method> JSON_GET_PRO_METHOD_MAP = new HashMap<>();

    static {
        List<String> importMethods = ListUtil.of("getString", "getBoolean", "getLong", "getInteger", "inStr", "notBlank");
        final Method[] methods = ReflectUtil.getMethods(JSONUtil.class);
        if (ArrayUtil.isNotEmpty(methods)) {
            Method res = null;
            for (Method method : methods) {
                if (importMethods.contains(method.getName())) {
                    //排除协变桥接方法，pr#1965@Github
                    if (res == null || res.getReturnType().isAssignableFrom(method.getReturnType())) {
                        res = method;
                        JSON_GET_PRO_METHOD_MAP.put(method.getName(), method);
                    }
                }
            }
        }
    }

    /**
     * 使用EL获取动态值
     *
     * @param parameterNames 参数名称数组
     * @param args           参数
     * @param expression     表达式
     * @param clazz          返回类型class
     * @param methodMap      注册到上下文的方法map
     * @param <T>            返回类型泛型
     * @return 返回值
     */
    public static <T> T getDynamicValue(String[] parameterNames, Object[] args, String expression, Class<T> clazz, Map<String, Method> methodMap) {
        if (ArrayUtil.isEmpty(parameterNames)) {
            return null;
        }
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        boolean registerJsonFun = false;
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
            if (!registerJsonFun && args[i] instanceof JSONObject) {
                JSON_GET_PRO_METHOD_MAP.forEach(context::registerFunction);
                registerJsonFun = true;
            }
        }
        if (MapUtil.isNotEmpty(methodMap)) {
            methodMap.forEach(context::registerFunction);
        }
        return parser.parseExpression(expression).getValue(context, clazz);
    }


}
