package org.group1418.easy.escm.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.group1418.easy.escm.common.base.obj.BasePageQo;
import org.group1418.easy.escm.common.wrapper.PageR;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author yq 2024/1/2 14:54
 * @description PageUtil 分页查询工具
 */
public class PageUtil {

    /**
     * 分页查询包装
     *
     * @param qo     查询参数
     * @param helper 获取数据方法
     * @param <T>    返回类型
     * @return 分页查询结果
     */
    public static <T, Q extends BasePageQo> PageR<T> select(Q qo, QueryHelper<T, Q> helper) {
        return select(qo, helper, null);
    }

    /**
     * 分页查询包装
     *
     * @param qo      查询参数
     * @param helper  获取数据方法
     * @param countId 自定义countId 在mapper中自定义查询total的方法
     * @param <T>     返回类型
     * @return 分页查询结果
     */
    public static <T, Q extends BasePageQo> PageR<T> select(Q qo, QueryHelper<T, Q> helper, String countId) {
        //自定义分页参数转 mybatis-plus分页参数
        Page<T> page = new Page<>();
        page.setSize(ObjectUtil.defaultIfNull(qo.getSize(), BasePageQo.DEFAULT_SIZE));
        page.setCurrent(ObjectUtil.defaultIfNull(qo.getPage(), BasePageQo.DEFAULT_PAGE));
        if (StrUtil.isNotBlank(countId)) {
            page.setCountId(countId);
        }
        //获得查询结果
        IPage<T> pageResult = helper.query(page, qo);
        //包装为自定义分页结果
        PageR<T> pageR = new PageR<>();
        pageR.setPages(pageResult.getPages());
        pageR.setTotal(pageResult.getTotal());
        pageR.setRows(pageResult.getRecords());
        return pageR;
    }

    /**
     * 分页查询包装,查询所有
     *
     * @param qo     查询参数
     * @param helper 获取数据方法
     * @param <T>    返回类型
     * @return 分页查询结果
     */
    public static <T, Q extends BasePageQo> List<T> selectAll(Q qo, QueryHelper<T, Q> helper) {
        //自定义分页参数转 mybatis-plus分页参数
        Page<T> page = new Page<>();
        page.setSize(-1);
        page.setCurrent(BasePageQo.DEFAULT_PAGE);
        //获得查询结果
        IPage<T> pageResult = helper.query(page, qo);
        return pageResult.getRecords();
    }

    /**
     * 分页查询包装
     *
     * @param qo     查询参数
     * @param helper 获取数据方法
     * @param <T>    返回类型
     * @return 分页查询结果
     */
    public static <T, Q extends BasePageQo> PageR<T> selectWithLoop(Q qo, QueryHelper<T, Q> helper, Consumer<T> singleDataConsumer) {
        return selectWithLoop(qo, helper, singleDataConsumer, null);
    }

    /**
     * 分页查询包装
     *
     * @param qo                 查询参数
     * @param helper             获取数据方法
     * @param singleDataConsumer 单条数据处理
     * @param countId            自定义countId,在mapper中自定义查询total的方法
     * @param <T>                返回类型
     * @return 分页查询结果
     */
    public static <T, Q extends BasePageQo> PageR<T> selectWithLoop(Q qo, QueryHelper<T, Q> helper, Consumer<T> singleDataConsumer, String countId) {
        PageR<T> pageR = select(qo, helper, countId);
        if (CollUtil.isNotEmpty(pageR.getRows()) && singleDataConsumer != null) {
            pageR.getRows().forEach(singleDataConsumer);
        }
        return pageR;
    }


    public interface QueryHelper<T, Q extends BasePageQo> {
        /**
         * 分页查询
         *
         * @param page 分页参数
         * @param qo   查询参数
         * @return 查询结果
         */
        IPage<T> query(Page<T> page, Q qo);
    }
}
