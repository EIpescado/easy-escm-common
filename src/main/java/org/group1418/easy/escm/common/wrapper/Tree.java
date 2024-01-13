package org.group1418.easy.escm.common.wrapper;


import cn.hutool.core.collection.CollectionUtil;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author yq
 * @date 2019/07/23 11:33
 * @description 树
 * @since V1.0.0
 */
public class Tree<ID,T extends TreeNode<ID,T>> {

    private final ID rootId;

    public Tree(ID rootId) {
        this.rootId = rootId;
    }

    /**
     * 构建树
     * @param list 数据集合
     * @return 树
     */
    public List<T> build(List<T> list){
        if(CollectionUtil.isNotEmpty(list)){
            //按父菜单分组
            Map<ID,List<T>> map = list.stream().filter(n -> n != null && n.getEnabled()).peek(n ->{
                if(n.getPid() == null){
                    n.setPid(rootId);
                }
            }).collect(Collectors.groupingBy(TreeNode::getPid));
            //顶级菜单
            List<T> baseMenus = map.get(rootId);
            return loop(baseMenus,map);
        }
        return null;
    }

    /**
     * 构建树
     * @param list 数据集合
     * @param parser 额外的单节点消费处理
     * @return 树
     */
    public List<T> build(List<T> list, Consumer<T> parser){
        if(CollectionUtil.isNotEmpty(list)){
            //按父菜单分组
            Map<ID,List<T>> map = list.stream().filter(n -> n != null && n.getEnabled()).peek(n ->{
                if(n.getPid() == null){
                    n.setPid(rootId);
                }
                parser.accept(n);
            }).collect(Collectors.groupingBy(TreeNode::getPid));
            //顶级菜单
            List<T> baseMenus = map.get(rootId);
            return loop(baseMenus,map);
        }
        return null;
    }

    private  List<T> loop(List<T> list,Map<ID,List<T>> map){
        for (T node : list){
            node.setChildren(map.get(node.getId()));
            if(CollectionUtil.isNotEmpty(node.getChildren())){
                loop(node.getChildren(),map);
            }
        }
        return list;
    }

}
