package org.group1418.easy.escm.common.wrapper;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import lombok.Data;

/**
 * @author yq
 * @date 2020年9月17日 15:32:32
 * @description select object,下拉框对象
 * @since V1.0.0
 */
@Data
public class Selector<T> {

    private String label;

    @JSONField(serializeUsing = ToStringSerializer.class)
    private T value;

    public static <T> Selector<T> of(String label, T value) {
        Selector<T> selector = new Selector<>();
        selector.setLabel(label);
        selector.setValue(value);
        return selector;
    }
}
