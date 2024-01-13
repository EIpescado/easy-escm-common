package org.group1418.easy.escm.common.utils;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author yq 2024/1/2 15:20
 * @description ReUtil 正则工具
 */
public class RegexUtil {

    /**
     * 移动电话
     */
    private final static Pattern MOBILE = Pattern.compile("(?:0|86|\\+86)?1[2-9]\\d{9}");
    /**
     * 手机或电话正则
     */
    private static final Pattern TEL_AND_PHONE_PATTERN = Pattern.compile("1[0-9]\\d{9}|0\\d{2,3}-[1-9]\\d{6,7}");
    /**
     * 座机号码
     */
    private final static Pattern TEL = Pattern.compile("((\\d{11})|^((\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d)|(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d))$)");
    /**
     * 邮箱
     */
    private static final Pattern MAIL_PATTERN = Pattern.compile("^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");
    /**
     * 数字正则
     */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)");
    /**
     * 数字字母汉字
     */
    public static final Pattern NUMBER_LETTER_CN_PATTERN = Pattern.compile("[^0-9a-zA-Z\u4e00-\u9fa5]");

    /**
     * 是否为手机号
     *
     * @param str 字符串
     * @return 是否手机
     */
    public static boolean isMobile(String str) {
        return ReUtil.isMatch(MOBILE, str);
    }

    /**
     * 是否电话
     *
     * @param str 字符串
     * @return 是否电话
     */
    public static boolean isTel(String str) {
        return ReUtil.isMatch(TEL, str);
    }

    /**
     * 从字符串中获取所有手机和固话
     *
     * @param str 字符串
     * @return 手机和固话
     */
    public static List<String> getTelAndPhone(String str) {
        if (StrUtil.isBlank(str)) {
            return null;
        }
        Matcher matcher = TEL_AND_PHONE_PATTERN.matcher(str.trim().replaceAll(" ", ""));
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 是否邮箱
     *
     * @param str 字符串
     * @return 是否邮箱
     */
    public static boolean isMail(String str) {
        if (StrUtil.isBlank(str)) {
            return false;
        }
        return ReUtil.isMatch(MAIL_PATTERN, str);
    }

    /**
     * 是否电话或邮箱
     *
     * @param str 字符串
     * @return 是否电话/邮箱
     */
    public static boolean isPhoneOrMail(String str) {
        return isMobile(str) || isMail(str);
    }

    /**
     * 从字符串中提取第一个数字
     *
     * @param str 字符串
     * @return 数字
     */
    public static String getNumberGroup0(String str) {
        if (StrUtil.isBlank(str)) {
            return null;
        }
        return ReUtil.getGroup0(NUMBER_PATTERN, str);
    }

    /**
     * 是否包含中文
     * @param str 字符串
     * @return 是否包含中文
     */
    public static boolean isContainChinese(String str) {
        return ReUtil.contains(ReUtil.RE_CHINESES, str);
    }

    /**
     * 移除字符中除数字,字母,中文之外的所有字符
     * @param str 字符串
     * @return 结果
     */
    public static String removeAllSpecialChar(String str) {
        if (StrUtil.isBlank(str)) {
            return str;
        }
        return RegexUtil.NUMBER_LETTER_CN_PATTERN.matcher(str).replaceAll("");
    }

    /**
     * 获取指定文本中所有匹配正则字段,转为json对象
     *
     * @param pattern 子模式别名正则,形如 \d+(?<time>\d{4}-\d{2}-\d{2} \d{2}:\d{2})(?<invoiceNo>\d{20})
     * @param text    待匹配文本
     * @return 结果集
     */
    public static List<JSONObject> getAllGroups(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        List<JSONObject> result = new ArrayList<>();
        // 通过反射获取 namedGroups 方法
        final Map<String, Integer> map = ReflectUtil.invoke(pattern, "namedGroups");
        //正则没写好会导致死循环,添加最大匹配限制
        int limit = 1993;
        while (matcher.find() && limit > 0) {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, Integer> pe : map.entrySet()) {
                jsonObject.put(pe.getKey(), matcher.group(pe.getValue()));
            }
            result.add(jsonObject);
            limit--;
        }
        return result;
    }


    public static void main(String[] args) {
        System.out.println(removeAllSpecialChar("S等  22待的1颿提.."));
    }
}
