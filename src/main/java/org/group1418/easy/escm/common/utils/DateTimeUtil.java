package org.group1418.easy.escm.common.utils;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * @author yq 2023/12/29 11:28
 * @description DateUtil
 */
public class DateTimeUtil {

    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter YYYY_MM_DD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter YMDHMS_DATE_PATTERN = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter YMD_DATE_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter HMS_TIME_PATTERN = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter HM_TIME_PATTERN = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter YYYY_MM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter YMD_HM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59);
    private static final Pattern Y_M_D_PATTERN = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}");
    private static final Pattern Y_M_D_H_M_S_PATTERN = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}");


    private DateTimeUtil() {
    }

    /**
     * 转化日期
     *
     * @param date yyyy-MM-dd 日期字符串
     * @return 日期
     */
    public static LocalDate parseDate(String date) {
        if (StrUtil.isBlank(date)) {
            return null;
        }
        return LocalDateTimeUtil.parseDate(date, YYYY_MM_DD_FORMATTER);
    }

    /**
     * 转化日期,无值则默认
     *
     * @param date yyyy-MM-dd 日期字符串
     * @param defaultDate 无值则使用默认值
     * @return 日期
     */
    public static LocalDate parseDate(String date, LocalDate defaultDate) {
        LocalDate localDate = parseDate(date);
        return localDate != null ? localDate : defaultDate;
    }

    /**
     * 获取指定日期的一天开始时间
     *
     * @param date yyyy-MM-dd 日期字符串
     * @return 一天的开始, 即yyyy-MM-dd 00:00:00
     */
    public static LocalDateTime getStartOfDay(String date) {
        LocalDate localDate = parseDate(date);
        return localDate != null ? localDate.atStartOfDay() : null;
    }

    /**
     * 获取指定日期的一天结束时间
     *
     * @param date yyyy-MM-dd 日期字符串
     * @return 一天的开始, 即yyyy-MM-dd 23:59:59
     */
    public static LocalDateTime getEndOfDay(String date) {
        LocalDate localDate = parseDate(date);
        return localDate != null ? localDate.atTime(END_OF_DAY) : null;
    }

    /**
     * 获取指定日期的一天结束时间
     *
     * @param date yyyy-MM-dd 日期字符串
     * @return 一天的开始, 即yyyy-MM-dd 23:59:59
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(END_OF_DAY);
    }

    /**
     * 判定指定时间是否在有效期内
     *
     * @param judgeTime 判定的时间
     * @param startDate 有效期
     * @param endDate   失效期
     * @return 是否有效
     */
    public static boolean timeValid(LocalDateTime judgeTime, LocalDate startDate, LocalDate endDate) {
        if (judgeTime == null) {
            return false;
        }
        if (startDate == null) {
            if (endDate != null) {
                return judgeTime.isBefore(getEndOfDay(endDate));
            }
            return false;
        } else {
            if (endDate == null) {
                return judgeTime.isAfter(startDate.atStartOfDay());
            }
            return judgeTime.isAfter(startDate.atStartOfDay()) && judgeTime.isBefore(getEndOfDay(endDate));
        }
    }

    /**
     * 转化时间
     *
     * @param time 时间字符串  yyyy-MM-dd HH:mm:ss
     * @return 时间
     */
    public static LocalDateTime parseTime(String time) {
        if (StrUtil.isBlank(time)) {
            return null;
        }
        return LocalDateTimeUtil.parse(time, DEFAULT_FORMATTER);
    }

    /**
     * 转化日期
     *
     * @param date 日期
     * @return 日期字符串 yyyy-MM-dd
     */
    public static String formatDate(LocalDate date) {
        return LocalDateTimeUtil.format(date, YYYY_MM_DD_FORMATTER);
    }

    /**
     * 转化日期(紧密)
     *
     * @param date 日期
     * @return 日期字符串 yyyyMMdd
     */
    public static String formatDateTrim(LocalDate date) {
        return LocalDateTimeUtil.format(date, YMD_DATE_PATTERN);
    }


    /**
     * 转化日期
     *
     * @param date yyyyMMdd 日期字符串
     * @return 日期
     */
    public static LocalDate parseDateTrim(String date) {
        if (StrUtil.isBlank(date)) {
            return null;
        }
        return LocalDateTimeUtil.parseDate(date, YMD_DATE_PATTERN);
    }

    /**
     * 转化日期到年月
     *
     * @param date 日期
     * @return 日期字符串 yyyy-MM
     */
    public static String formatDate2Month(LocalDate date) {
        return LocalDateTimeUtil.format(date, YYYY_MM_FORMATTER);
    }

    /**
     * 转化时间
     *
     * @param time 时间
     * @return 时间字符串 yyyy-MM-dd HH:mm:ss
     */
    public static String formatTime(LocalDateTime time) {
        return LocalDateTimeUtil.format(time, DEFAULT_FORMATTER);
    }

    /**
     * 转化时间,只到分
     *
     * @param time 时间
     * @return 时间字符串 yyyy-MM-dd HH:mm:ss
     */
    public static String formatTime2M(LocalDateTime time) {
        return LocalDateTimeUtil.format(time, YMD_HM_FORMATTER);
    }

    /**
     * 转化时间,紧密
     *
     * @param time 时间
     * @return 时间字符串 yyyyMMddHHmmss
     */
    public static String formatTimeTrim(LocalDateTime time) {
        return LocalDateTimeUtil.format(time, YMDHMS_DATE_PATTERN);
    }

    /**
     * 转化时间为日期字符串
     *
     * @param time 时间
     * @return 日期字符串 yyyy-MM-dd
     */
    public static String formatTime2Date(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return LocalDateTimeUtil.format(time.toLocalDate(), YYYY_MM_DD_FORMATTER);
    }

    /**
     * 转化时间为时分秒
     *
     * @param time 时间
     * @return 时分秒字符串
     */
    public static String formatTimeHms(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return LocalDateTimeUtil.format(time, HMS_TIME_PATTERN);
    }

    /**
     * 转化时间为时分秒,支持 HH:mm 或 HH:mm:ss
     *
     * @param time 时间
     * @return 时分秒字符串
     */
    public static LocalTime parseTimeHms(String time) {
        if (StrUtil.isBlank(time)) {
            return null;
        }
        DateTimeFormatter formatter = time.length() == 5 ? HM_TIME_PATTERN : HMS_TIME_PATTERN;
        return LocalTime.parse(time, formatter);
    }

    /**
     * 是否为日期字符串 yyyy-MM-dd
     */
    public static boolean isDate(String str) {
        if (StrUtil.isBlank(str)) {
            return false;
        }
        return Y_M_D_PATTERN.matcher(str).matches();
    }

    /**
     * 是否为日期字符串 yyyy-MM-dd HH:mm:ss
     */
    public static boolean isTime(String str) {
        if (StrUtil.isBlank(str)) {
            return false;
        }
        return Y_M_D_H_M_S_PATTERN.matcher(str).matches();
    }

}
