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
     * 用户ID
     */
    private Long currentUserId;

    /**
     * 关键字搜索,用于全局搜索
     */
    private String keyword;

    /**
     * 所属客户ID
     */
    private Long currentCustomerId;

    /**
     * 登录组织
     */
    private String currentOrganization;

    /**
     * 是否主账号
     */
    private Boolean master;

    /**
     * 导出字段
     */
    private List<String> exportCols;
}
