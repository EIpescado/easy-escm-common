package org.group1418.easy.escm.common.exception;


/**
 * @author yq
 * @date 2020/09/21 12:04
 * @description 系统自定义异常
 * @since V1.0.0
 */
public class SystemCustomException extends CustomException {

    private static final long serialVersionUID = -6632920847550172761L;

    public SystemCustomException(String format, Object... args) {
        super(format, args);
    }
}
