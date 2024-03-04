package org.group1418.easy.escm.common.validator.annotation;


import org.group1418.easy.escm.common.validator.StrCheckValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据校验
 *
 * @author yq 2021年4月27日 14:33:55
 */
@Constraint(validatedBy = {StrCheckValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrCheck {

    /**
     * 字段名
     */
    String name();

    /**
     * 提示信息
     */
    String message() default "";

    /**
     * 是否必填
     */
    boolean required() default true;

    /**
     * 数据在指定范围
     */
    String[] in() default {};

    /**
     * 最大长度
     */
    int maxLength() default -1;

    /**
     * 固定长度
     */
    int fixedLength() default -1;

    StringType type() default StringType.NONE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 数据类型
     */
    enum StringType {
        /**
         * 无规则
         */
        NONE,
        /**
         * 手机
         */
        PHONE,
        /**
         * 邮箱
         */
        MAIL,
        /**
         * 电话
         */
        TEL,
        /**
         * 不包含中文
         */
        NO_CHINESE,
        /**
         * yyyy-MM-dd 日期
         */
        DATE,
        /**
         * yyyy-MM-dd HH:mm:ss 时间
         */
        TIME,
        /**
         * 手机或邮箱
         */
        PHONE_OR_MAIL
    }
}
