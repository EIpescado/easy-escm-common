package org.group1418.easy.escm.common.wrapper;

import cn.hutool.core.util.ArrayUtil;
import lombok.Data;
import org.group1418.easy.escm.common.exception.SystemCustomException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * @author yq 2021/4/27 16:36
 * @description ValidResult
 */
@Data
public class ValidResult implements Serializable {

    private static final long serialVersionUID = 7792117038622168318L;
    private String fieldName;
    private boolean pass;
    private String message;

    public ValidResult(String fieldName) {
        this.fieldName = fieldName;
    }

    public ValidResult of(boolean pass, Object... args) {
        if (ArrayUtil.isNotEmpty(args)) {
            StringBuilder sb = new StringBuilder(fieldName);
            sb.append(" ");
            Arrays.stream(args).forEach(sb::append);
            this.message = sb.toString();
        }
        this.pass = pass;
        return this;
    }

    public ValidResult pass(Object... args) {
        return of(true, args);
    }

    public ValidResult no(Object... args) {
        return of(false, args);
    }

    public void notPassThen(Consumer<String> messageConsumer) {
        if (messageConsumer != null && !pass) {
            messageConsumer.accept(message);
        }
    }

    public void notPassThrow() {
        if (!pass) {
            throw new SystemCustomException(message);
        }
    }

}
