package org.group1418.easy.escm.common.validator;


import org.group1418.easy.escm.common.utils.ValidateUtils;
import org.group1418.easy.escm.common.validator.annotation.StrCheck;
import org.group1418.easy.escm.common.wrapper.ValidResult;

/**
 * @author yq
 * @date 2020/08/04 10:59
 * @description 数据校验
 * @since V1.0.0
 */
public class StrCheckValidator extends AbstractCheckValidator<StrCheck, String> {

    private String name;
    private boolean required;
    private String[] dataArray;
    private int maxLength;
    private int fixedLength;
    private StrCheck.StringType type;

    @Override
    public void initialize(StrCheck constraintAnnotation) {
        this.name = constraintAnnotation.name();
        this.required = constraintAnnotation.required();
        this.dataArray = constraintAnnotation.in();
        this.maxLength = constraintAnnotation.maxLength();
        this.fixedLength = constraintAnnotation.fixedLength();
        this.type = constraintAnnotation.type();
    }

    @Override
    public ValidResult getValidResult(String value) {
        return ValidateUtils.checkData(value, name, required, dataArray, maxLength, fixedLength, type);
    }
}
