package org.group1418.easy.escm.common.utils;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.group1418.easy.escm.common.exception.SystemCustomException;
import org.group1418.easy.escm.common.validator.annotation.StrCheck;
import org.group1418.easy.escm.common.wrapper.ValidResult;

import java.math.BigDecimal;

/**
 * @author yq 2024/2/27 11:23
 * @description ValidateUtils 校验工具类
 */
public class ValidateUtils {

    public static ValidResult maxLength(String value, String fieldName, boolean required, int maxLength) {
        return checkData(value, fieldName, required, null, maxLength, -1, StrCheck.StringType.NONE);
    }

    public static String maxLengthTrim(String value, String fieldName, boolean required, int maxLength) {
        String val = PudgeUtil.full2HalfWithTrim(value);
        checkData(val, fieldName, required, null, maxLength, -1, StrCheck.StringType.NONE).notPassThrow();
        return val;
    }

    public static ValidResult in(String value, String fieldName, boolean required, String[] dataArray) {
        return checkData(value, fieldName, required, dataArray, -1, -1, StrCheck.StringType.NONE);
    }

    public static ValidResult inPro(String value, String fieldName, boolean required, String... dataArray) {
        return checkData(value, fieldName, required, dataArray, -1, -1, StrCheck.StringType.NONE);
    }

    public static ValidResult checkData(String value, String fieldName,
                                        boolean required, String[] dataArray,
                                        int maxLength,
                                        int fixedLength,
                                        StrCheck.StringType type) {
        ValidResult result = new ValidResult(fieldName);
        if (StrUtil.isEmpty(value) || StrUtil.isEmpty(value.trim())) {
            return result.of(!required, "必填");
        } else {
            //超出最大长度
            if (maxLength != -1 && value.length() > maxLength) {
                return result.no("最大长度", maxLength);
            }
            //不为指定长度
            if (fixedLength != -1 && value.length() != fixedLength) {
                return result.no("固定长度", fixedLength);
            }
            //不在指定范围内
            if (ArrayUtil.isNotEmpty(dataArray) && !ArrayUtil.containsAny(dataArray, value)) {
                return result.no("无效");
            }
            if (type != null && StrCheck.StringType.NONE != type) {
                boolean pass;
                String tip = "无效";
                switch (type) {
                    case PHONE:
                        pass = RegexUtil.isMobile(value);
                        break;
                    case MAIL:
                        pass = RegexUtil.isMail(value);
                        break;
                    case TEL:
                        pass = RegexUtil.isTel(value);
                        break;
                    case NO_CHINESE:
                        pass = !RegexUtil.isContainChinese(value);
                        tip = "不可包含中文";
                        break;
                    case DATE:
                        pass = DateTimeUtil.isDate(value);
                        break;
                    case TIME:
                        pass = DateTimeUtil.isTime(value);
                        break;
                    case PHONE_OR_MAIL:
                        pass = RegexUtil.isPhoneOrMail(value);
                        break;
                    default:
                        pass = true;
                }
                return result.of(pass, tip);
            }
            return result.pass();
        }
    }

    public static ValidResult checkBigDecimal(BigDecimal value, String fieldName, boolean required, int precision, int scale, boolean gtZero) {
        ValidResult result = new ValidResult(fieldName);
        if (value == null) {
            return result.of(!required, "必填");
        } else {
            if (value.precision() > precision) {
                return result.no("最大长度为", precision);
            }
            if (value.scale() > scale) {
                return result.no("最大小数位为", scale);
            }
            if (gtZero && !NumberUtil.isGreater(value, BigDecimal.ZERO)) {
                return result.no("必须大于0");
            }
        }
        return result.pass();
    }

    public static BigDecimal parseBigDecimal(String str, String fieldName, boolean required, int precision, int scale, boolean gtZero) {
        String strTrim = StrUtil.removeAll(StrUtil.blankToDefault(PudgeUtil.full2HalfWithTrim((str)), ""), ',', '，');
        boolean notBlank = StrUtil.isNotBlank(strTrim);
        if (notBlank && !NumberUtil.isNumber(strTrim)) {
            throw new SystemCustomException(fieldName + "无效");
        }
        BigDecimal result = notBlank ? new BigDecimal(strTrim) : null;
        checkBigDecimal(result, fieldName, required, precision, scale, gtZero).notPassThrow();
        return result;
    }
}
