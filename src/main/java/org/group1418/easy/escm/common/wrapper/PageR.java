package org.group1418.easy.escm.common.wrapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.PageUtil;
import lombok.Data;
import org.group1418.easy.escm.common.base.obj.BasePageQo;

import java.io.Serializable;
import java.util.List;

/**
 * 分页包装类 根据前端需要变更
 * @author yq
 */
@Data
public class PageR<T> implements Serializable {

    private static final long serialVersionUID = -2047195214836327951L;
    /**
     * 总条数
     */
    private long total;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 数据
     */
    private List<T> rows;

    public static <T> PageR<T> empty(){
        PageR<T> r = new PageR<>();
        r.setTotal(0);
        r.setPages(0);
        r.setRows(null);
        return r;
    }

    public static <T> PageR<T> pageRows(List<T> rows,Long size){
        PageR<T> backR = new PageR<>();
        backR.setRows(rows);
        backR.setTotal(CollUtil.size(rows));
        backR.setPages(PageUtil.totalPage(backR.getTotal(), size != null ? size.intValue() : BasePageQo.DEFAULT_SIZE.intValue()));
        return backR;
    }

    public static <T> PageR<T> unionR(List<T> rows,Long size,long total){
        PageR<T> backR = new PageR<>();
        backR.setRows(rows);
        backR.setTotal(total);
        backR.setPages(PageUtil.totalPage(total, size != null ? size.intValue() : BasePageQo.DEFAULT_SIZE.intValue()));
        return backR;
    }
}
