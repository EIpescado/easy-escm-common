package org.group1418.easy.escm.common.base.obj;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author yq
 * @date 2024年3月8日 14:36:47
 * @description 基础租户Entity
 * @since V1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantEntity extends BaseEntity {

    /**
     * 租户ID
     */
    private String tenantId;

}
