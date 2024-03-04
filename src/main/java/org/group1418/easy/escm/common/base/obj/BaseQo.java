package org.group1418.easy.escm.common.base.obj;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yq
 * @date 2021年4月14日 10:42:31
 * @description 查询对象基类
 * @since V1.0.0
 */
@Data
public class BaseQo implements Serializable {

    private static final long serialVersionUID = -5414534189079839739L;

    /**
     * 关键字搜索,用于全局搜索
     */
    private String keyword;

    /**
     * 当前用户ID
     */
    private Long currentUid;

    /**
     * 用户所属租户ID
     */
    private Long tenantId;

    /**
     * 当前登录组织(公司旗下多个子公司,区分当前操作组织)
     */
    private String currentOrg;

}
