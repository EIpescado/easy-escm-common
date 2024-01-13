package org.group1418.easy.escm.common.base.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.group1418.easy.escm.common.base.BaseService;
import org.group1418.easy.escm.common.base.CommonMapper;
import org.group1418.easy.escm.common.base.LambdaQueryWrapperX;
import org.group1418.easy.escm.common.base.obj.BaseEntity;

import java.util.List;
import java.util.function.Consumer;


/**
 * @author yq
 * @date 2018/11/15 11:39
 * @description 基础实现
 * @since V1.0.0
 */
@Slf4j
public class BaseServiceImpl<M extends CommonMapper<T>, T extends BaseEntity> extends ServiceImpl<M, T> implements BaseService<T> {

    /**
     * mybatis 使用 lambda更新时 FastjsonTypeHandler
     */
    protected final String FAST_JSON_TYPE_HANDLER_MAPPING = "typeHandler=com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler";

    @Override
    public boolean haveFieldValueEq(SFunction<T, ?> function, Object value) {
        Long count = baseMapper.selectCount(Wrappers.<T>lambdaQuery().eq(function, value));
        return count != null && count > 0;
    }

    @Override
    public boolean haveMatchData(Consumer<LambdaQueryWrapperX<T>> consumer) {
        LambdaQueryWrapperX<T> lambdaQueryWrapper = new LambdaQueryWrapperX<>();
        consumer.accept(lambdaQueryWrapper);
        Long count = baseMapper.selectCount(lambdaQueryWrapper);
        return count != null && count > 0;
    }

    @Override
    public T getOneByFieldValueEq(SFunction<T, ?> function, Object value) {
        return baseMapper.selectOne(Wrappers.<T>lambdaQuery().eq(function, value));
    }

    @Override
    public T getOneByWrapper(Consumer<LambdaQueryWrapperX<T>> consumer) {
        LambdaQueryWrapperX<T> wrapper = new LambdaQueryWrapperX<>();
        consumer.accept(wrapper);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public List<T> getByFieldValueEq(SFunction<T, ?> function, Object value) {
        return baseMapper.selectList(Wrappers.<T>lambdaQuery().eq(function, value));
    }

    @Override
    public List<T> getByWrapper(Consumer<LambdaQueryWrapperX<T>> consumer) {
        LambdaQueryWrapperX<T> wrapper = new LambdaQueryWrapperX<>();
        consumer.accept(wrapper);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public Integer deleteByFieldEq(SFunction<T, ?> function, Object value) {
        return baseMapper.delete(Wrappers.<T>lambdaQuery().eq(function, value));
    }

    @Override
    public Integer deleteByWrapper(Consumer<LambdaQueryWrapperX<T>> consumer) {
        LambdaQueryWrapperX<T> wrapper = new LambdaQueryWrapperX<>();
        consumer.accept(wrapper);
        return baseMapper.delete(wrapper);
    }

    @Override
    public void updateByWrapper(Consumer<LambdaUpdateWrapper<T>> consumer) {
        LambdaUpdateWrapper<T> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
        consumer.accept(lambdaUpdateWrapper);
        this.update(lambdaUpdateWrapper);
    }

    @Override
    public Long countByWrapper(Consumer<LambdaQueryWrapperX<T>> consumer) {
        LambdaQueryWrapperX<T> wrapper = new LambdaQueryWrapperX<>();
        consumer.accept(wrapper);
        return baseMapper.selectCount(wrapper);
    }
}
