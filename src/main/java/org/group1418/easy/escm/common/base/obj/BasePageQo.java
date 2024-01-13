package org.group1418.easy.escm.common.base.obj;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author yq
 * @date 2021年4月14日 10:42:23
 * @description 分页查询对象 基类
 * @since V1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BasePageQo extends BaseQo {

    private static final long serialVersionUID = 6482628540038042720L;
    public static final Long DEFAULT_PAGE = 1L;
    public static final Long DEFAULT_SIZE = 10L;
    /**
     * 页数
     */
    protected Long page = DEFAULT_PAGE;

    /**
     * 单页显示总条数
     */
    protected Long size = DEFAULT_SIZE;
}
