package org.group1418.easy.escm.common.wrapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;


/**
 * @author yq
 * @date 2020/09/25 14:52
 * @description 菜单树节点
 * @sinceV1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MenuTreeNode extends TreeNode<Long, MenuTreeNode> {
    private static final long serialVersionUID = 8337076770624687962L;

    /**
     * 路由名称
     */
    private String name;

    /**
     * 组件地址
     */
    private String component;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 路由参数
     */
    private String query;

    /**
     * 是否隐藏
     */
    private Boolean hidden;

    /**
     * 是否外链菜单
     */
    private Boolean iFrame;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * meta data
     */
    private MenuMeta meta;

    /**
     * 是否为button
     */
    private Boolean beButton;

    @Data
    public static class MenuMeta {
        /**
         * 菜单名称
         */
        private String title;
        /**
         * 菜单图标
         */
        private String icon;
        /**
         * 菜单是否缓存 keep-alive
         */
        private Boolean cached;
        /**
         * 是否固定在 tag-view中
         */
        private Boolean affix;
        /**
         * 是否在面包屑中显示
         */
        private Boolean breadCrumb;
        /**
         * 当路由设置了该属性，则会高亮相对应的侧边栏
         * 这在某些场景非常有用，比如：一个文章的列表页路由为：/article/list
         * 点击文章进入文章详情页，这时候路由为/article/1，但你想在侧边栏高亮文章列表的路由，就可以进行如下设置
         */
        private String activeMenu;

        /**
         * 菜单下的按钮
         */
        private Map<String, List<ButtonNode>> buttons;
    }

}
