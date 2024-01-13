package org.group1418.easy.escm.common.aop;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.group1418.easy.escm.common.annotation.OpLog;
import org.group1418.easy.escm.common.base.IMQService;
import org.group1418.easy.escm.common.base.obj.BasePageQo;
import org.group1418.easy.escm.common.exception.SystemCustomException;
import org.group1418.easy.escm.common.spring.SpringExpressionParser;
import org.group1418.easy.escm.common.utils.PudgeUtil;
import org.group1418.easy.escm.common.wrapper.OpLogDto;
import org.group1418.easy.escm.common.wrapper.R;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;


/**
 * 切面定义
 *
 * @author by yq on 2021年7月3日 20:40:19
 */
@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class CommonAspect {

    private static final ThreadLocal<Long> CURRENT_TIME = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_PARAMS = new ThreadLocal<>();
    private final IMQService mqService;

    /**
     * 查询参数 转化aop,自动带入当前用户参数
     */
    @Around("execution(public * org.group1418.easy.escm..*.*(org.group1418.easy.escm.common.base.obj.BasePageQo+,*))")
    public Object aroundPageQoPro(ProceedingJoinPoint pjp) throws Throwable {
        return around(pjp);
    }

    private Object around(ProceedingJoinPoint pjp) throws Throwable {
        BasePageQo qo = (BasePageQo) pjp.getArgs()[0];
        if (qo != null && qo.getCurrentUserId() == null) {
            try {
//                CustomUserDetails customUserDetails = CustomUserDetails.currentDetails();
//                qo.setCurrentUserId(customUserDetails.getUserId());
//                Long customerId = customUserDetails.getCustomerId();
//                qo.setCurrentCustomerId(customUserDetails.getCustomerId());
//                OrganizationEnum organization = customUserDetails.getOrganization();
//                qo.setCurrentOrganization(organization.name());
//                qo.setCurrentOrganizationErpId(organization.getEasCompanyId());
//                //非E链主账号 实现主/子账号
//                boolean notEscmMaster = true;
//                if (customerId != null) {
//                    notEscmMaster = !customUserDetails.getCurrentUser().getBeEscmMaster();
//                }
//                qo.setNotEscmMaster(notEscmMaster);
            } catch (SystemCustomException systemCustomException) {
                //部分列表不登录也可访问
            }
        }
        return pjp.proceed();
    }

    /**
     * 定义日志切点
     */
    @Pointcut("@annotation(org.group1418.easy.escm.common.annotation.OpLog)")
    public void logPointcut() {
    }

    /**
     * 日志环绕增强
     */
    @Around("logPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        CURRENT_TIME.set(System.currentTimeMillis());
        //原始参数
        setParamsLocal(joinPoint);
        result = joinPoint.proceed();
        generateLogAndSend(joinPoint, false, result);
        return result;
    }

    /**
     * 配置异常通知
     */
    @AfterThrowing(pointcut = "logPointcut()", throwing = "throwable")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable throwable) {
        generateLogAndSend(joinPoint, true, R.fail(throwable.getLocalizedMessage()));
    }

    private void setParamsLocal(JoinPoint joinPoint) {
        JSONObject params = null;
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            final Method method = signature.getMethod();
            OpLog annotation = method.getAnnotation(OpLog.class);
            if (annotation.saveParams()) {
                HttpServletRequest currentRequest = this.currentRequest();
                if (currentRequest != null) {
                    params = getParams(currentRequest, method, joinPoint.getArgs());
                }
            }
            CURRENT_PARAMS.set(params != null ? params.toJSONString() : null);
        } catch (Exception ignored) {
        }
    }

    private void generateLogAndSend(JoinPoint joinPoint, boolean fail, Object result) {
        try {
            long timeCost = System.currentTimeMillis() - CURRENT_TIME.get();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            final Method method = signature.getMethod();
            OpLog annotation = method.getAnnotation(OpLog.class);
            OpLogDto dto = OpLogDto.buildByAnnotation(annotation, Long.valueOf(timeCost).intValue(), annotation.hadLogin(), fail);
            String paramsStr = CURRENT_PARAMS.get();
            if (annotation.saveParams()) {
                dto.setParams(paramsStr);
            }
            if (annotation.saveResult() && result != null) {
                dto.setResult(JSON.toJSONString(result));
            }
            //过滤日志表达式不为空
            if (StrUtil.isNotBlank(annotation.condition())) {
                JSONObject params = StrUtil.contains(annotation.condition(), "#params") ? JSON.parseObject(paramsStr) : new JSONObject();
                //获取方法参数名称
                Parameter[] parameters = method.getParameters();
                String[] paramNames = null;
                if (ArrayUtil.isNotEmpty(parameters)) {
                    paramNames = Arrays.stream(method.getParameters()).map(Parameter::getName).toArray(String[]::new);
                }
                paramNames = ArrayUtil.append(paramNames, "params", "result");
                Object[] args = ArrayUtil.append(joinPoint.getArgs(), params, result);
                Boolean saveLog = SpringExpressionParser.getDynamicValue(paramNames, args, annotation.condition(), Boolean.class, null);
                if (!BooleanUtil.isTrue(saveLog)) {
                    return;
                }
            }
            mqService.sendOpLogMessage(dto);
        } catch (Exception ignored) {
//            log.error("记录操作日志异常[{}]", ignored.getLocalizedMessage());
        } finally {
            CURRENT_TIME.remove();
            CURRENT_PARAMS.remove();
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return sra != null ? sra.getRequest() : null;
    }

    /**
     * 获取请求参数
     *
     * @param currentRequest 当前请求
     * @param method         方法
     * @param args           参数
     * @return 请求参数json
     */
    private JSONObject getParams(HttpServletRequest currentRequest, Method method, Object[] args) {
        JSONObject requestParams = (JSONObject) JSON.toJSON(currentRequest.getParameterMap());
        Object requestBody = this.getRequestBody(method, args);
        if (requestBody == null && requestParams == null) {
            return null;
        }
//        Span span = this.tracer.currentSpan();
        JSONObject json = new JSONObject()
                .fluentPut("ip", PudgeUtil.getIp(currentRequest))
                .fluentPut("path", currentRequest.getRequestURL().toString())
                .fluentPut("m", currentRequest.getMethod())
                .fluentPut("ua", currentRequest.getHeader("User-Agent"));
//                .fluentPut("traceId", span != null ? span.context().traceIdString() : "")
//                .fluentPut("auth", currentRequest.getHeader(CustomSecurityProperties.header));
        if (requestBody != null) {
            //requestBody
            json.put("rb", requestBody);
        }
        if (MapUtil.isNotEmpty(requestParams)) {
            //默认的格式是 单参数为key:["A"],修改为 key: "A"
            requestParams.entrySet().forEach(entry -> {
                JSONArray array = requestParams.getJSONArray(entry.getKey());
                entry.setValue(CollUtil.isNotEmpty(array) ? array.getString(0) : null);
            });
            //requestParams
            json.put("rp", requestParams);
        }
        return json;
    }

    /**
     * 获取请求体
     *
     * @param method 方法
     * @param args   参数
     * @return requestBody
     */
    private Object getRequestBody(Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        Parameter[] parameters = method.getParameters();
        RequestBody requestBody;
        for (int i = 0, length = parameters.length; i < length; i++) {
            requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                return args[i];
            }
        }
        return null;
    }
}
