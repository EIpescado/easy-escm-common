package org.group1418.easy.escm.common.enums.system;

/**
 * @author yq 2021年9月24日 10:10:19
 * @description 状态枚举, 启用禁用
 */
public enum AbleStateEnum {
    /**
     * 状态
     */
    ON("启用"),
    OFF("禁用"),
    ;

    String state;

    AbleStateEnum(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return state;
    }
}
