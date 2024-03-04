package org.group1418.easy.escm.common.validator;


import org.group1418.easy.escm.common.wrapper.ValidResult;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;

/**
 * @author yq 2021/4/27 17:34
 * @description AbstractCheckValidator
 */
public abstract class AbstractCheckValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {

    @Override
    public boolean isValid(T value, ConstraintValidatorContext context) {
        //禁用默认message
        context.disableDefaultConstraintViolation();
        ValidResult result = getValidResult(value);
        context.buildConstraintViolationWithTemplate(result.getMessage()).addConstraintViolation();
        return result.isPass();
    }

    /**
     * 根据值产生对应校验结果
     *
     * @param value 校验的值
     * @return 校验结果
     */
    public abstract ValidResult getValidResult(T value);
}
