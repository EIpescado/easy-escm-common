package org.group1418.easy.escm.common.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.group1418.easy.escm.common.base.obj.BaseEntity;

/**
 * @author yq
 * @date 2021年4月14日 10:43:20
 * @description 通用mapper
 * @since V1.0.0
 */
public interface CommonMapper<T extends BaseEntity> extends BaseMapper<T> {

}
