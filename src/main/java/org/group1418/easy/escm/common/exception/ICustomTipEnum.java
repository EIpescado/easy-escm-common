package org.group1418.easy.escm.common.exception;


import org.group1418.easy.escm.common.wrapper.CustomTip;

/**
 * @author yq
 * @date 2019/01/30 16:53
 * @description 提示枚举接口
 * @since V1.0.0
 */
public interface ICustomTipEnum {

    /**
     * 提示
     * @return tip
     */
    CustomTip tip();

    /**
     * 提示编码
     * @return 提示编码
     */
    default String getCode() {
        return tip().getCode();
    }

    /**
     * 提示信息
     * @return 提示信息
     */
    default String getMsg() {
        return tip().getMsg();
    }

}
