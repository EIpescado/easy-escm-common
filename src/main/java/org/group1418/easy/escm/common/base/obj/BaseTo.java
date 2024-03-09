package org.group1418.easy.escm.common.base.obj;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yq
 * @date 2021年4月14日 10:42:01
 * @description base to 列表对象
 * @since V1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BaseTo implements Serializable {

    private static final long serialVersionUID = 6526236438185395534L;
    /**
     * ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 版本
     */
    private Integer version;

    /**
     * 创建用户ID
     */
    private Long createUserId;
    /**
     * 创建用户
     */
    private String createUser;

    /**
     * 最后修改用户ID
     */
    private Long updateUserId;
    /**
     * 最后修改用户
     */
    private String updateUser;

    /**
     * 租户ID
     */
    private Long tenantId;
}
