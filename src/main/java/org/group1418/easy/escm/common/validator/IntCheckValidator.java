package org.group1418.easy.escm.common.validator;


import cn.hutool.core.util.ArrayUtil;
import org.group1418.easy.escm.common.validator.annotation.IntCheck;
import org.group1418.easy.escm.common.wrapper.ValidResult;

import java.util.Arrays;


/**
 * @author yq
 * @date 2020/08/04 10:59
 * @description 数据校验
 * @since V1.0.0
 */
public class IntCheckValidator extends AbstractCheckValidator<IntCheck, Integer> {

    private String name;
    private boolean required;
    private String[] dataArray;
    private int max;
    private int min;

    @Override
    public void initialize(IntCheck constraintAnnotation) {
        this.name = constraintAnnotation.name();
        this.required = constraintAnnotation.required();
        this.dataArray = constraintAnnotation.in();
        this.max = constraintAnnotation.max();
        this.min = constraintAnnotation.min();
    }

    @Override
    public ValidResult getValidResult(Integer value) {
        ValidResult result = new ValidResult(name);
        if (value == null) {
            return result.of(!required, "必填");
        } else {
            //小于最小值
            if (value < min) {
                return result.no("最小值", min);
            }
            //超出最大值
            if (value > max) {
                return result.no("最大值", max);
            }
            //不在指定范围内
            if (ArrayUtil.isNotEmpty(dataArray) && Arrays.stream(dataArray).noneMatch(i -> i.equals(value.toString()))) {
                return result.no("无效");
            }
            return result.pass();
        }
    }
}
