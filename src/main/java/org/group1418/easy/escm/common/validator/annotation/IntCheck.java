package org.group1418.easy.escm.common.validator.annotation;


import org.group1418.easy.escm.common.validator.IntCheckValidator;

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
@Constraint(validatedBy = {IntCheckValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IntCheck {

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
     * 最大值
     */
    int max() default Integer.MAX_VALUE;

    /**
     *  最小值
     */
    int min() default Integer.MIN_VALUE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
