package org.group1418.easy.escm.common.validator;


import org.group1418.easy.escm.common.utils.ValidateUtils;
import org.group1418.easy.escm.common.validator.annotation.BigDecimalCheck;
import org.group1418.easy.escm.common.wrapper.ValidResult;

import java.math.BigDecimal;

/**
 * @author yq
 * @date 2020/08/04 10:59
 * @description 数据校验
 * @since V1.0.0
 */
public class BigDecimalCheckValidator extends AbstractCheckValidator<BigDecimalCheck, BigDecimal> {

    private String name;
    private boolean required;
    private int precision;
    private int scale;
    private boolean gtZero;

    @Override
    public void initialize(BigDecimalCheck constraintAnnotation) {
        this.name = constraintAnnotation.name();
        this.required = constraintAnnotation.required();
        this.precision = constraintAnnotation.precision();
        this.scale = constraintAnnotation.scale();
        this.gtZero = constraintAnnotation.gtZero();
    }

    @Override
    public ValidResult getValidResult(BigDecimal value) {
        return ValidateUtils.checkBigDecimal(value, name, required, precision, scale, gtZero);
    }

}
