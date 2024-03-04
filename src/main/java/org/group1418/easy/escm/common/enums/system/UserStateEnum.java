package org.group1418.easy.escm.common.enums.system;

/**
 * 用户状态枚举
 * @author yq 2021年5月13日 10:24:14
 */
public enum UserStateEnum {
    /**状态*/
    NORMAL("正常"),
    FORBIDDEN("禁用"),
    NOT_ACTIVATED("未激活"),
    ;

    String userState;

    UserStateEnum(String userState) {
        this.userState = userState;
    }

    @Override
    public String toString() {
        return userState;
    }
}
