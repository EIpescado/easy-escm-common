package org.group1418.easy.escm.common.config;

import cn.dev33.satoken.exception.SaTokenException;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.group1418.easy.escm.common.enums.CustomTipEnum;
import org.group1418.easy.escm.common.exception.CustomException;
import org.group1418.easy.escm.common.utils.PudgeUtil;
import org.group1418.easy.escm.common.wrapper.R;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

/**
 * controller  全局异常回调
 *
 * @author yq on 2020年9月17日 15:06:25
 */
@RestControllerAdvice
@Slf4j
public class GlobalDefaultExceptionHandler {

    public GlobalDefaultExceptionHandler() {
        log.info("注入 GlobalDefaultExceptionHandler");
    }

    /**
     * 全局异常
     */
    @ExceptionHandler(Throwable.class)
    public R<String> defaultErrorHandler(Throwable e) {
        Throwable cause = e.getCause();
        if (cause instanceof CustomException) {
            return R.fail(((CustomException) cause).getTip());
        }
        log.error("[{}]未知的异常 [{}]", currentRequestUrl(), StrUtil.removeAllLineBreaks(e.getMessage()), e);
        return R.fail(CustomTipEnum.FAIL);
    }

    /**
     * 您的主机中的软件中止了一个已建立的连接
     */
    @ExceptionHandler(ClientAbortException.class)
    public R<String> clientAbortExceptionHandler(ClientAbortException e) {
        log.error("[{}]连接被终止:[{}]", currentRequestUrl(), e.getLocalizedMessage());
        return R.fail(CustomTipEnum.FAIL);
    }

    /**
     * 请求参数缺失
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<String> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException e) {
        log.info("[{}][{}]缺失", currentRequestUrl(), e.getParameterName());
        return R.fail("[{}]缺失", e.getParameterName());
    }

    /**
     * 请求参数缺失
     */
    @ExceptionHandler(BindException.class)
    public R<String> bindExceptionExceptionHandler(BindException e) {
        return getExceptionDefaultMessage(e.getBindingResult(), e);
    }

    /**
     * 请求体缺失
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<String> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        log.error("[{}]无有效请求体:[{}]", currentRequestUrl(), e.getLocalizedMessage());
        return R.fail(e.getLocalizedMessage());
    }


    /**
     * 请求体缺失
     */
    @ExceptionHandler(MultipartException.class)
    public R<String> multipartExceptionExceptionHandler(MultipartException e) {
        log.error("[{}]无有效请求体:[{}]", currentRequestUrl(), e.getLocalizedMessage());
        return R.fail(e.getLocalizedMessage());
    }

    /**
     * 全局参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<String> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        return getExceptionDefaultMessage(e.getBindingResult(), e);
    }

    private R<String> getExceptionDefaultMessage(BindingResult bindingResult, Exception e) {
        List<ObjectError> errors = bindingResult.getAllErrors();
        if (CollectionUtil.isNotEmpty(errors)) {
            //返回第一个错误的信息
            Optional<ObjectError> optional = errors.stream().filter(error -> StrUtil.isNotEmpty(error.getDefaultMessage())).findFirst();
            return optional.map(objectError -> {
                String message;
                if (objectError instanceof FieldError) {
                    FieldError fieldError = (FieldError) objectError;
                    String field = fieldError.getField();
                    //取出 field如 details[0].test 中的index
                    String rowNumber = ReUtil.getGroup1(PudgeUtil.SQUARE_BRACKETS_PATTERN, field);
                    message = objectError.getDefaultMessage();
                    if (StrUtil.isNotEmpty(rowNumber)) {
                        message = StrBuilder.create("第[", Integer.toString(Integer.parseInt(rowNumber) + 1), "]条 ", message).toString();
                    }
                } else {
                    message = objectError.getDefaultMessage();
                }
                log.info("[{}]校验不通过[{}]", currentRequestUrl(), message);
                return R.<String>fail(message);
            }).orElseGet(() -> R.fail("校验异常"));
        } else {
            log.error("[{}]自定义校验错误: [{}]", currentRequestUrl(), e.getLocalizedMessage());
            return R.fail(CustomTipEnum.FAIL);
        }
    }

    /**
     * spring 抛出的异常
     */
    @ExceptionHandler(NestedRuntimeException.class)
    public R<String> nestedRuntimeExceptionHandler(NestedRuntimeException e) {
        log.info("[{}] spring 异常 [{}]", currentRequestUrl(), StrUtil.removeAllLineBreaks(e.getLocalizedMessage()));
        return R.fail(CustomTipEnum.FAIL);
    }

    /**
     * 405
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<String> httpRequestMethodNotSupportedExceptionHandle(HttpRequestMethodNotSupportedException e) {
        log.info("[{}] method [{}] not allow", currentRequestUrl(), e.getMethod());
        return R.fail(CustomTipEnum.METHOD_NOT_ALLOWED);
    }

    /**
     * 415
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public R<String> httpMediaTypeNotSupportedExceptionHandle(HttpMediaTypeNotSupportedException e) {
        log.info("[{}] not support [{}]", currentRequestUrl(), e.getContentType());
        return R.fail(CustomTipEnum.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * 404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public R<String> noHandlerFoundExceptionHandle(NoHandlerFoundException e) {
        log.info("[{}] not found [{}]", currentRequestUrl(),e.getRequestURL());
        return R.fail(CustomTipEnum.NOT_FOUND);
    }

    /**
     * SA-token异常
     */
    @ExceptionHandler(SaTokenException.class)
    public R<String> customExceptionHandler(SaTokenException e) {
        return R.fail(e.getCode(),e.getMessage());
    }

    /**
     * 自定义异常回调
     */
    @ExceptionHandler(CustomException.class)
    public R<String> customExceptionHandler(CustomException e) {
        return R.fail(e.getTip());
    }


    private String currentRequestUrl() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (sra != null) {
            HttpServletRequest request = sra.getRequest();
            log.info("IP[{}]", PudgeUtil.getIp(request));
            return request.getRequestURI();
        }
        return null;
    }
}
