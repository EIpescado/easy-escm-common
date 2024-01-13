package org.group1418.easy.escm.common.wrapper;


import cn.hutool.core.lang.func.VoidFunc0;
import cn.hutool.core.text.StrFormatter;
import lombok.Data;
import org.group1418.easy.escm.common.enums.CustomTipEnum;
import org.group1418.easy.escm.common.exception.ICustomTipEnum;
import org.group1418.easy.escm.common.exception.SystemCustomException;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 返回结果
 *
 * @author yq 2018年12月27日 16:03:56
 */
@Data
public class R<T> implements Serializable {

    /**
     * 返回结果 编码 0：成功 1：失败
     */
    private String code;
    /**
     * 返回结果 描述信息
     */
    private String message;
    /**
     * 返回结果
     */
    private T res;

    public R() {
    }

    public R(String code, String message, T res) {
        this.code = code;
        this.message = message;
        this.res = res;
    }

    public R(String code, String message) {
        this.code = code;
        this.message = message;
        this.res = null;
    }

    public R(ICustomTipEnum customTipEnum, T res) {
        this.code = customTipEnum.getCode();
        this.message = customTipEnum.getMsg();
        this.res = res;
    }

    /**
     * 判断返回是否成功
     */
    public boolean succeed() {
        return code.equals(CustomTipEnum.SUCCESS.getCode());
    }

    /**
     * 返回成功
     */
    public static <T> R<T> ok(T res) {
        return new R<>(CustomTipEnum.SUCCESS, res);
    }

    public static <T> R<T> ok() {
        return new R<>(CustomTipEnum.SUCCESS, null);
    }

    /**
     * 返回失败
     */
    public static <T> R<T> fail(String message) {
        return new R<>(CustomTipEnum.FAIL.getCode(), message, null);
    }

    public static <T> R<T> fail(String message, T res) {
        return new R<>(CustomTipEnum.FAIL.getCode(), message, res);
    }

    public static <T> R<T> fail(ICustomTipEnum tip) {
        return new R<>(tip.getCode(), tip.getMsg());
    }

    public static <T> R<T> fail(CustomTip tip) {
        return new R<>(tip.getCode(), tip.getMsg());
    }

    public static R<String> formatFail(String format, Object... args) {
        return new R<>(CustomTipEnum.FAIL.getCode(), StrFormatter.format(format, args));
    }

    public void then(Consumer<T> okConsumer, VoidFunc0 catchFun) {
        if (succeed()) {
            okConsumer.accept(this.res);
        } else {
            catchFun.callWithRuntimeException();
        }
    }

    public void thenWithThrow(Consumer<T> okConsumer) {
        if (succeed()) {
            okConsumer.accept(this.res);
        } else {
            throw new SystemCustomException(this.message);
        }
    }

    public void then(Consumer<T> okConsumer, Consumer<String> errorMessageConsumer) {
        if (succeed()) {
            okConsumer.accept(this.res);
        } else {
            errorMessageConsumer.accept(this.message);
        }
    }

    public <BR> BR thenBackWithThrow(Function<T, BR> okFunction) {
        if (succeed()) {
            return okFunction.apply(this.res);
        } else {
            throw new SystemCustomException(this.message);
        }
    }
}
