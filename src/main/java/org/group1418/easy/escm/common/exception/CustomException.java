package org.group1418.easy.escm.common.exception;

import cn.hutool.core.text.StrFormatter;
import org.group1418.easy.escm.common.wrapper.CustomTip;
import org.group1418.easy.escm.common.enums.CustomTipEnum;

/**
 * @author yq
 * @date 2019/05/21 11:34
 * @description 自定义异常
 * @since V1.0.0
 */
public class CustomException extends RuntimeException {
    private static final long serialVersionUID = -4083494081772087464L;

    protected CustomTip tip;

    public CustomException(CustomTip customTip) {
        super(customTip.getMsg());
        this.tip = customTip;
    }

    public CustomException(String format, Object... args) {
        this(CustomTip.of(CustomTipEnum.FAIL.getCode(), StrFormatter.format(format, args)));
    }

    public CustomTip getTip() {
        return tip;
    }
}
