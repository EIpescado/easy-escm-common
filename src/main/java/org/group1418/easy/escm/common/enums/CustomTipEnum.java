package org.group1418.easy.escm.common.enums;


import org.group1418.easy.escm.common.wrapper.CustomTip;
import org.group1418.easy.escm.common.exception.ICustomTipEnum;

/**
 * @author yq
 * @date 2021年4月13日 16:48:24
 * @description 提示枚举
 * @since V1.0.0
 */
public enum CustomTipEnum implements ICustomTipEnum {
    /**
     * 通用异常
     */
    SUCCESS(0, "success"),
    FAIL(1, "fail"),
    CREDENTIALS_INVALID(10001,"凭证无效或已过期"),
    REFRESH_CREDENTIALS_INVALID(10002, "刷新凭证无效或已过期"),
    PERMISSION_DENIED(10110,"无权访问"),

    /**
     * 服务器异常
     */
    NOT_FOUND(404, "404,not found"),
    METHOD_NOT_ALLOWED(405, "405,method not allowed"),
    UNSUPPORTED_MEDIA_TYPE(415, "415,Unsupported Media Type"),
    SERVER_ERROR(500, "500,server error"),
    ;

    CustomTip tip;

    CustomTipEnum(int code, String msg) {
        this.tip = CustomTip.of(code, msg);
    }

    @Override
    public CustomTip tip() {
        return tip;
    }

}
