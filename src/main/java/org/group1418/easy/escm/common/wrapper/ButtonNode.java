package org.group1418.easy.escm.common.wrapper;

import lombok.Data;

/**
 * @author yq
 * @date 2020/09/25 17:11
 * @description 按钮
 * @since V1.0.0
 */
@Data
public class ButtonNode {

    private Long id;

    private String buttonName;

    private Integer sortNo ;

    private String icon;

    private String click;

    private Long pid;

    private String position;

    private Boolean enabled;
}
