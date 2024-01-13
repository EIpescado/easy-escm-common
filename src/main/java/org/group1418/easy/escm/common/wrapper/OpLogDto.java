package org.group1418.easy.escm.common.wrapper;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.group1418.easy.escm.common.annotation.OpLog;
import org.group1418.easy.escm.common.exception.SystemCustomException;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yq 2022/10/12 17:29
 * @description OpLogData 操作日志
 */
@Data
public class OpLogDto implements Serializable {
    private static final long serialVersionUID = -17388513313461536L;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口耗时
     */
    private int timeCost;

    /**
     * 参数
     */
    private String params;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 租户ID
     */
    private Long tenantId;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    /**
     * 接口请求是否失败
     */
    private Boolean fail;

    /**
     * 用户客户端类型
     */
    private String clientType;

    /**
     * 响应内容
     */
    private String result;

    /**
     * 是否对外
     */
    private Boolean outer;

    public static OpLogDto buildByAnnotation(OpLog opLog, int timeCost, boolean hadLogin, boolean fail) {
        OpLogDto dto = new OpLogDto();
        dto.setName(opLog.value());
        dto.setTimeCost(timeCost);
        if (hadLogin) {
            try {
//                CustomUserDetails customUserDetails = CustomUserDetails.currentDetails();
//                dto.setUserId(customUserDetails.getUserId());
//                dto.setCustomerId(customUserDetails.getCustomerId());
//                dto.setClientType(customUserDetails.getClientType().name());
            } catch (SystemCustomException customException) {
                //可能登陆过期 获取不到
            }
        }
        dto.setTime(LocalDateTime.now());
        dto.setFail(fail);
        dto.setOuter(opLog.outer());
        return dto;
    }
}
