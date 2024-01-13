package org.group1418.easy.escm.common.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;
import org.group1418.easy.escm.common.utils.PudgeUtil;

/**
 * @author yq
 * @date 2020/07/03 14:09
 * @description 布尔值转中文 是/否
 * @since V1.0.0
 */
public class BooleanCnConverter implements Converter<Boolean> {


    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Boolean> context) throws Exception {
        Boolean val = context.getValue();
        return new WriteCellData<>(PudgeUtil.boolToChinese(val));
    }

}
