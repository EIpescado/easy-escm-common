package org.group1418.easy.escm.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.converters.AutoConverter;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.group1418.easy.escm.common.exception.CustomException;
import org.group1418.easy.escm.common.exception.SystemCustomException;
import org.group1418.easy.escm.common.wrapper.R;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yq
 * @date 2022年3月14日 10:42:43
 * @description excel工具
 * @since V1.0.0
 */
@Slf4j
public class ExcelUtil {

    private static final String XLSX = ".xlsx";

    /**
     * 导出xlsx
     *
     * @param realFileName 导出文件名称
     * @param response     响应流
     * @param exportFun    导出fun
     */
    public static void exportXlsx(String realFileName, HttpServletResponse response, ExportFun exportFun) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            //文件名称
            String fileName = URLEncoder.encode(realFileName, "UTF-8");
            if (!fileName.contains(XLSX)) {
                fileName = fileName + XLSX;
            }
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            exportFun.execute(response.getOutputStream());
        } catch (IOException e) {
            log.info("导出xlsx异常[{}]", e.getLocalizedMessage());
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(PudgeUtil.APPLICATION_JSON_UTF_8);
            response.setCharacterEncoding("UTF-8");
            try {
                response.getWriter().append(JSON.toJSONString(R.fail(e.getLocalizedMessage())));
                response.getWriter().flush();
                response.getWriter().close();
            } catch (IOException ioException) {
                log.info("导出xlsx响应异常[{}]", e.getLocalizedMessage());
            }
        }
    }

    /**
     * 导出xlsx
     *
     * @param data         数据
     * @param clazz        class
     * @param realFileName 文件名,可不带后缀
     */
    public static <T> void exportXlsx(List<T> data, Class<T> clazz, String realFileName, HttpServletResponse response) {
        exportXlsx(realFileName, response, outputStream -> exportXlsx(data, clazz, outputStream, null));
    }

    /**
     * 导出xlsx
     *
     * @param data                    数据
     * @param clazz                   class
     * @param outputStream            输出流
     * @param excludeColumnFiledNames 不需要导出的字段名
     */
    private static <T> void exportXlsx(List<T> data, Class<T> clazz, OutputStream outputStream, List<String> excludeColumnFiledNames) {
        ExcelWriterSheetBuilder sheet1 = EasyExcel.write(outputStream, clazz).sheet("sheet1");
        if (CollUtil.isNotEmpty(excludeColumnFiledNames)) {
            sheet1.excludeColumnFiledNames(excludeColumnFiledNames);
        }
        sheet1.doWrite(data);
    }

    /**
     * 导出xlsx
     *
     * @param headList                表头
     * @param data                    数据
     * @param outputStream            输出流
     * @param excludeColumnFiledNames 不需要导出的字段名
     */
    private static void exportXlsx(List<List<String>> headList, List<List<Object>> data, OutputStream outputStream, List<String> excludeColumnFiledNames) {
        ExcelWriterSheetBuilder sheet1 = EasyExcel.write(outputStream).sheet("sheet1");
        if (CollUtil.isNotEmpty(excludeColumnFiledNames)) {
            sheet1.excludeColumnFiledNames(excludeColumnFiledNames);
        }
        if (CollUtil.isNotEmpty(headList)) {
            sheet1.head(headList);
        }
        sheet1.doWrite(data);
    }


    /**
     * 自定义字段及顺序导出xlsx
     *
     * @param data         数据
     * @param clazz        class
     * @param realFileName 文件名,可不带后缀
     * @param exportCols   需要导出的字段,按此顺序
     */
    @SuppressWarnings("unchecked")
    public static <T> void exportXlsx(List<T> data, Class<T> clazz, String realFileName, List<String> exportCols, HttpServletResponse response) {
        log.info("用户导出[{}],自定义字段[{}]个", clazz.getSimpleName(), CollUtil.size(exportCols));
        if (CollUtil.isEmpty(exportCols)) {
            exportXlsx(data, clazz, realFileName, response);
        } else {
            //动态表头
            final List<List<String>> headList = new ArrayList<>(exportCols.size());
            //需要导出字段 在类中的属性对象
            List<Field> exportFieldList = new ArrayList<>(exportCols.size());
            //读取类中所有属性
            Map<String, Field> dataFieldMap = Arrays.stream(ReflectUtil.getFields(clazz)).collect(Collectors.toMap(Field::getName, Function.identity(), (a, b) -> a));
            //部分字段可能配置了 @ExcelProperty 的converter 或 @DateTimeFormat
            Map<String, DateTimeFormatter> timeFormatMap = new HashMap<>(dataFieldMap.size());
            Map<String, Converter<?>> converterMap = new HashMap<>(dataFieldMap.size());
            //防止重复new converter对象
            Map<String, Converter<?>> converterClassMap = new HashMap<>(dataFieldMap.size());
            //循环获取属性和描述,保证顺序
            exportCols.forEach(col -> {
                Field field = dataFieldMap.get(col);
                if (field != null) {
                    ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
                    if (annotation != null) {
                        //日期类型 额外转化,可能配了注解
                        if (field.getType().equals(LocalDateTime.class)) {
                            DateTimeFormat dateAnnotation = field.getAnnotation(DateTimeFormat.class);
                            if (dateAnnotation != null) {
                                timeFormatMap.put(col, DateTimeFormatter.ofPattern(dateAnnotation.value()));
                            } else {
                                //默认日期格式化
                                timeFormatMap.put(col, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            }
                        }
                        //配置了 @ExcelProperty 的converter,即自定义转化类
                        if (!annotation.converter().equals(AutoConverter.class)) {
                            Converter<?> converter = PudgeUtil.getAndPutIfNotExist(converterClassMap, annotation.converter().getName(),
                                    converterName -> ReflectUtil.newInstance(annotation.converter()));
                            converterMap.put(col, converter);
                        }
                        headList.add(ListUtil.of(annotation.value()[0]));
                        exportFieldList.add(field);
                    }
                }
            });
            if (CollUtil.isEmpty(data)) {
                exportXlsx(realFileName, response, outputStream -> exportXlsx(headList, new ArrayList<>(), outputStream, null));
            } else {
                //动态数据
                final List<List<Object>> newDataList = data.stream().map(row -> {
                    List<Object> rowDataList = new ArrayList<>(exportFieldList.size());
                    exportFieldList.forEach(field -> {
                        DateTimeFormatter dateTimeFormatter = timeFormatMap.get(field.getName());
                        Object fieldValue = ReflectUtil.getFieldValue(row, field);
                        if (dateTimeFormatter != null) {
                            if (fieldValue != null) {
                                rowDataList.add(LocalDateTimeUtil.format((LocalDateTime) fieldValue, dateTimeFormatter));
                            } else {
                                rowDataList.add(null);
                            }
                        } else {
                            //支持自定义converter
                            Converter<Object> converter = (Converter<Object>) converterMap.get(field.getName());
                            if (converter != null) {
                                WriteConverterContext<Object> writeConverterContext = new WriteConverterContext<>(fieldValue, null, null);
                                try {
                                    WriteCellData<?> writeCellData = converter.convertToExcelData(writeConverterContext);
                                    rowDataList.add(writeCellData.getStringValue());
                                } catch (Exception e) {
                                    log.info("[{}]自定义converter转化异常,使用原值", field.getName());
                                    rowDataList.add(fieldValue);
                                }
                            } else {
                                // 枚举默认使用toString
                                if (fieldValue instanceof Enum) {
                                    rowDataList.add(fieldValue.toString());
                                } else {
                                    rowDataList.add(fieldValue);
                                }
                            }
                        }
                    });
                    return rowDataList;
                }).collect(Collectors.toList());
                exportXlsx(realFileName, response, outputStream -> exportXlsx(headList, newDataList, outputStream, null));
            }
        }
    }

    @Data
    public static class CustomWriteHandler implements CellWriteHandler {
        private CellWriteHandlerContext context;

        @Override
        public void afterCellCreate(CellWriteHandlerContext context) {
            CellWriteHandler.super.afterCellCreate(context);
            this.context = context;
        }
    }

    /**
     * 导入模版
     *
     * @param fileName            文件名
     * @param headRowNumber       解析起始行号
     * @param inputStreamSupplier 输入流supplier
     * @param clazz               一行数据的类型
     * @param rowDataConsumer     行数据消费函数
     * @param <T>                 行数据类型
     */
    public static <T> void importFile(String fileName, Integer headRowNumber,
                                      InputStreamSupplier inputStreamSupplier,
                                      Class<T> clazz,
                                      RowDataConsumer<T> rowDataConsumer) {
        InputStream inputStream = null;
        try {
            log.info("导入文件[{}]开始", fileName);
            inputStream = inputStreamSupplier.get();
            EasyExcel.read(inputStream, clazz, new ReadListener<T>() {
                @Override
                public void invoke(T data, AnalysisContext context) {
                    String noText = StrBuilder.create("第", Integer.toString(context.readRowHolder().getRowIndex() + 1), "行").toString();
                    rowDataConsumer.accept(data, context, noText);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("文件[{}]解析完毕", fileName);
                }

                @Override
                public void onException(Exception exception, AnalysisContext context) throws Exception {
                    if (exception instanceof CustomException) {
                        log.info("解析导入文件[{}]异常[{}]", fileName, exception.getLocalizedMessage());
                        throw exception;
                    } else if (exception instanceof ExcelDataConvertException) {
                        ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException) exception;
                        String tip = StrUtil.format("第[{}]行，第[{}]列解析异常", excelDataConvertException.getRowIndex() + 1, excelDataConvertException.getColumnIndex() + 1);
                        log.error("[{}]，数据为:[{}]", tip, excelDataConvertException.getCellData());
                        throw new SystemCustomException(tip);
                    } else {
                        log.error("文件解析异常:[{}]", exception.getLocalizedMessage());
                        throw new SystemCustomException("文件解析异常");
                    }
                }
            }).sheet().headRowNumber(headRowNumber).doRead();
        } catch (IOException e) {
            log.error("文件解析异常:[{}]", e.getLocalizedMessage());
            throw new SystemCustomException("文件解析异常");
        } finally {
            IoUtil.close(inputStream);
        }
    }

    @FunctionalInterface
    public interface InputStreamSupplier {

        /**
         * 获取流
         *
         * @return 输入流
         * @throws IOException
         */
        InputStream get() throws IOException;
    }

    @FunctionalInterface
    public interface ExportFun {
        void execute(OutputStream outputStream) throws IOException;
    }

    @FunctionalInterface
    public interface RowDataConsumer<T> {
        /**
         * 一行数据消费者
         *
         * @param t       行数据对象
         * @param context 解析上下文
         * @param noText  行描述, 即 第N行,方便前端显示
         */
        void accept(T t, AnalysisContext context, String noText);
    }
}

