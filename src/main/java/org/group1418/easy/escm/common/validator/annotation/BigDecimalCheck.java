package org.group1418.easy.escm.common.validator.annotation;


import org.group1418.easy.escm.common.validator.BigDecimalCheckValidator;

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
@Constraint(validatedBy = {BigDecimalCheckValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BigDecimalCheck {

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
     * 位数
     */
    int precision() default 18;

    /**
     * 小数最大位数
     */
    int scale() default 10;

    /**
     * 是否需要大于0
     */
    boolean gtZero() default true;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
