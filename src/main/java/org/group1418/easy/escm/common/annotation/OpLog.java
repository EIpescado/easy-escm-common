package org.group1418.easy.escm.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 日志注解 2022年10月12日 16:58:14
 * 参数必须使用spring注解标识, @RequestParam @RequestBody @ModelAttribute 参数才会被记录,@PathVariable不会被记录,已经包含在URL中
 *
 * @author yq
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OpLog {

    /**
     * 接口描述
     */
    String value() default "";

    /**
     * 是否保存请求参数
     *
     * @return 默认否
     */
    boolean saveParams() default true;

    /**
     * 是否保存响应结果
     *
     * @return 默认否
     */
    boolean saveResult() default true;

    /**
     * 是否是登录后的操作日志
     *
     * @return 默认是
     */
    boolean hadLogin() default true;

    /**
     * 日志是否对外,即用户操作日志,后续用于操作分析
     * @return 默认否
     */
    boolean outer() default false;

    /**
     * 表达式结果需返回boolean值,用此表达式结果来决定是否保存日志,默认为空即保存日志
     * 为效率考虑 若需要使用请求参数判定则需要保证将 saveParams 开启
     * 内置对象:
     *  rb, request body 参数
     *  rp, request params 参数
     *  result, 响应结果
     *  ip, IP
     *  ua, user agent
     * 内置方法(对jsonObject有效):
     *  详见 SpringExpressionParser.JSON_GET_PRO_METHOD_MAP
     * @return SpringEL  如  '1'.equals(#rp?.getString('keyword')), '1'.equals(#getString(#rp,'keyword'))
     */
    String condition() default "";
}
