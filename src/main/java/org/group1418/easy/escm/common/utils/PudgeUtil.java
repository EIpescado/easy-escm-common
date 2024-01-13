package org.group1418.easy.escm.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.crypto.symmetric.DES;
import com.alibaba.fastjson.JSON;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import lombok.extern.slf4j.Slf4j;
import org.group1418.easy.escm.common.wrapper.R;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author yq
 * @date 2021年4月14日 10:53:16
 * @description 一般工具
 * @since V1.0.0
 */
@Slf4j
public class PudgeUtil {

    private static final Pattern OVER_TWO_SPACE = Pattern.compile(" {2,}");
    private static final Digester SHA256_DIGESTER = new Digester(DigestAlgorithm.SHA256);
    private static final DES DES = SecureUtil.des();
    private static final Map<String, String[]> FILE_TYPE_MAP = new ConcurrentHashMap<String, String[]>() {
        private static final long serialVersionUID = 1997234982651928201L;

        {
            put("xls", new String[]{"doc", "msi"});
            put("zip", new String[]{"docx", "xlsx", "pptx", "jar", "war"});
        }
    };
    /**
     * 中括号正则
     */
    public static final Pattern SQUARE_BRACKETS_PATTERN = Pattern.compile("\\[(.*?)]");
    /**
     * application-json
     */
    public static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    /**
     * IP可能的请求头
     */
    private static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR",
            "X-Real-IP"
    };
    private final static String UN_KNOWN = "unknown";
    private final static List<String> LOCALHOST_IP_LIST = CollectionUtil.newArrayList("127.0.0.1", "0:0:0:0:0:0:0:1");
    private final static Pattern STR_CHANGE_PRO_PATTERN = Pattern.compile("[^\\p{L}\\p{N}\\u4E00-\\u9FFF]|中国|省|市|县|镇|州|新|区");

    /**
     * 响应json
     * @param response 响应json
     * @param r r
     * @param <T> 返回结果类型
     * @throws IOException
     */
    public static <T> void responseJson(HttpServletResponse response, R<T> r) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(APPLICATION_JSON_UTF_8);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().append(JSON.toJSONString(r));
        response.getWriter().flush();
        response.getWriter().close();
    }


    /**
     * 解密 HEX + DES
     *
     * @param data 待解密数据
     * @return 明文
     */
    public static String decrypt(String data) {
        return DES.decryptStr(HexUtil.decodeHex(data));
    }

    /**
     * 加密 DES + HEX
     *
     * @param data 待加密数据
     * @return 密文
     */
    public static String encrypt(String data) {
        return HexUtil.encodeHexStr(DES.encrypt(data)).toUpperCase();
    }

    /**
     * 明文密码加密
     *
     * @param plainText 明文密码
     * @return 加密后密码
     */
    public static String encodePwd(String plainText) {
        return SHA256_DIGESTER.digestHex(plainText);
    }

    /**
     * 生成redisKey
     *
     * @param keys 建
     * @return 值
     */
    public static String buildKey(Object... keys) {
        if (ArrayUtil.isNotEmpty(keys)) {
            return StrUtil.join(StrUtil.COLON, keys);
        }
        return null;
    }

    /**
     * 取文件内容的前28个字节.判定文件类型
     *
     * @param bytes 文件内容字节数据
     * @return 文件类型
     */
    public static String getFileType(String fileName, byte[] bytes) {
        //取前28个字节
        byte[] bytes28 = ArrayUtil.sub(bytes, 0, 28);
        //根据文件内容获取的文件类型
        String typeName = FileTypeUtil.getType(HexUtil.encodeHexStr(bytes28, false));
        if (StrUtil.isNotBlank(typeName)) {
            String[] mayMatchTypeArray = FILE_TYPE_MAP.get(typeName);
            //部分文件根据文件内容读取类型与扩展名不符,需转化
            if (ArrayUtil.isNotEmpty(mayMatchTypeArray)) {
                String extName = FileUtil.extName(fileName);
                if (Arrays.stream(mayMatchTypeArray).anyMatch(s -> s.equalsIgnoreCase(extName))) {
                    return extName;
                }
            }
            return typeName;
        } else {
            //部分文件根据内容无法获取文件名 直接获取扩展名
            return FileUtil.extName(fileName);
        }
    }

    /**
     * url encode
     *
     * @param str 字符串
     * @return url结果
     */
    public static String urlEncode(String str) {
        try {
            return StrUtil.isNotEmpty(str) ? URLEncoder.encode(str, "UTF-8") : "";
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    /**
     * 格式化字符串
     * 例子  吃饭：{paramName}块
     *
     * @param template 模版
     * @param map      参数
     * @return 格式化后结果
     */
    public static String format(String template, Map<String, String> map) {
        if (StrUtil.isBlank(template)) {
            return template;
        }
        if (CollUtil.isNotEmpty(map)) {
            String result = template;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                result = StrUtil.replace(result, StrBuilder.create(StrUtil.DELIM_START, entry.getKey(), StrUtil.DELIM_END).toString(), StrUtil.nullToEmpty(entry.getValue()));
            }
            return result;
        }
        return template;
    }

    /**
     * 从map中获取元素,如果不存在则调用function 生成元素放入map
     *
     * @param map      map
     * @param key      key
     * @param function 生成v
     * @param <K>      key类型
     * @param <V>      值类型
     * @return 值
     */
    public static <K, V> V getAndPutIfNotExist(Map<K, V> map, K key, Function<K, V> function) {
        if (map == null) {
            return null;
        }
        if (ObjectUtil.isNotEmpty(key)) {
            V v = map.get(key);
            if (ObjectUtil.isEmpty(v)) {
                v = function.apply(key);
                map.put(key, v);
            }
            return v;
        }
        return null;
    }

    /**
     * 从map中获取指定key的当前值,执行function后将生成的新值覆盖存入map,
     *
     * @param map      map
     * @param key      key
     * @param function 生成v
     * @param <K>      key类型
     * @param <V>      value类型
     * @return 值
     */
    public static <K, V> V getAndPutAfterFun(Map<K, V> map, K key, Function<V, V> function) {
        if (map == null) {
            return null;
        }
        if (ObjectUtil.isNotEmpty(key)) {
            V lastV = map.get(key);
            V newV = function.apply(lastV);
            map.put(key, newV);
            return lastV;
        }
        return null;
    }

    /**
     * 全角转半角
     */
    public static String full2Half(String str) {
        return Convert.toDBC(str);
    }

    /**
     * 半角转全角
     */
    public static String half2full(String str) {
        return Convert.toSBC(str);
    }


    /**
     * 全角转半角,并移除前后空格
     */
    public static String full2HalfWithTrim(String str) {
        if (StrUtil.isBlank(str)) {
            return str;
        }
        return Convert.toDBC(str.trim());
    }

    /**
     * 从map获取指定key对应的值,没有则默认
     *
     * @param params     map
     * @param key        key
     * @param defaultStr 默认值
     * @return 值
     */
    public static String getStr(Map<String, String> params, String key, String defaultStr) {
        return StrUtil.nullToEmpty(MapUtil.getStr(params, key, defaultStr));
    }

    /**
     * 负数转0
     *
     * @param decimal 数字
     * @return 负数转0
     */
    public static BigDecimal negativeToZero(BigDecimal decimal) {
        if (decimal == null) {
            return null;
        }
        return NumberUtil.isLess(decimal, BigDecimal.ZERO) ? BigDecimal.ZERO : decimal;
    }

    /**
     * 布尔值转中文是/否
     *
     * @param b 布尔值
     * @return 是/否
     */
    public static String boolToChinese(Boolean b) {
        return BooleanUtil.isTrue(b) ? "是" : "否";
    }

    /**
     * 中文是/否转布尔值
     *
     * @param b 字符串 是/否
     * @return boolean
     */
    public static boolean chineseToBool(String b) {
        return "是".equals(b);
    }

    /**
     * 字符串转布尔
     *
     * @param b 字符串
     * @return 布尔值
     */
    public static Boolean s2b(String b) {
        if (StrUtil.isBlank(b)) {
            return null;
        }
        if ("1".equals(b)) {
            return true;
        } else if ("0".equals(b)) {
            return false;
        }
        return BooleanUtil.toBoolean(b);
    }

    public static boolean isStringHaveChange(String after, String before) {
        if (StrUtil.isBlank(before)) {
            return StrUtil.isNotBlank(after);
        }
        if (StrUtil.isNotBlank(after)) {
            return !StrUtil.trimToEmpty(after).equals(StrUtil.trimToEmpty(before));
        } else {
            return false;
        }
    }

    public static boolean isBooleanHaveChange(Boolean after, Boolean before) {
        if (before == null) {
            return after != null;
        }
        if (after != null) {
            return !after.equals(before);
        } else {
            return false;
        }
    }

    public static boolean isBigDecimalHaveChange(BigDecimal after, BigDecimal before) {
        BigDecimal a = NumberUtil.nullToZero(after);
        BigDecimal b = NumberUtil.nullToZero(before);
        return !NumberUtil.equals(a, b);
    }

    public static boolean isIntHaveChange(Integer after, Integer before) {
        if (before == null) {
            return after != null;
        }
        if (after != null) {
            return !NumberUtil.equals(after, before);
        } else {
            return false;
        }
    }

    public static boolean isLocalDateTimeHaveChange(LocalDateTime after, LocalDateTime before) {
        if (before == null) {
            return after != null;
        }
        if (after != null) {
            return !after.isEqual(before);
        } else {
            return false;
        }
    }

    public static boolean isLocalDateHaveChange(LocalDate after, LocalDate before) {
        if (before == null) {
            return after != null;
        }
        if (after != null) {
            return !after.isEqual(before);
        } else {
            return false;
        }
    }

    public static boolean isStringHaveChangePro(String after, String before) {
        if (StrUtil.isBlank(before)) {
            return StrUtil.isNotBlank(after);
        }
        if (StrUtil.isNotBlank(after)) {
            //去前后空格比较
            String aPro = StrUtil.trimToEmpty(after);
            String bPro = StrUtil.trimToEmpty(before);
            if (aPro.equals(bPro)) {
                return false;
            }
            //全转半角
            aPro = PudgeUtil.full2Half(aPro);
            bPro = PudgeUtil.full2Half(bPro);
            //先转半角比较
            if (aPro.equals(bPro)) {
                return false;
            }
            //转繁体比较
            return !ZhConverterUtil.toTraditional(aPro).equals(ZhConverterUtil.toTraditional(bPro));
        } else {
            return false;
        }
    }

    public static boolean isStringHaveChangeProMax(String after, String before) {
        if (StrUtil.isBlank(before)) {
            return StrUtil.isNotBlank(after);
        }
        if (StrUtil.isNotBlank(after)) {
            //移除前后空格,转半角,简体,小写,只保留英文,数字,汉字
            String aPro = transferStr(after);
            String bPro = transferStr(before);
            return !aPro.equals(bPro);
        } else {
            return false;
        }
    }

    public static boolean isAddressHaveChangePro(String a, String b) {
        if (!isStringHaveChange(a, b)) {
            return false;
        }
        return isStringHaveChangePro(StrUtil.removeAll(a, ' ', '\n', '\r'), StrUtil.removeAll(b, ' ', '\n', '\r'));
    }

    /**
     * 移除前后空格转半角转简体, 移除所有非中文,英文,数字, 指定字符,转小写
     *
     * @param str 原始字符串
     * @return 移除后字符串
     */
    public static String transferStr(String str) {
        return ReUtil.delAll(STR_CHANGE_PRO_PATTERN, ZhConverterUtil.toSimple(PudgeUtil.full2Half(StrUtil.trimToEmpty(str))).toLowerCase());
    }

    /**
     * 繁体转简体
     *
     * @param s 字符串
     * @return 简体
     */
    public static String toSimple(String s) {
        return StrUtil.isNotBlank(s) ? ZhConverterUtil.toSimple(s) : "";
    }

    /**
     * 是否大于0
     *
     * @param number 数字
     * @return 大于0
     */
    public static boolean gtZero(BigDecimal number) {
        return NumberUtil.isGreater(NumberUtil.nullToZero(number), BigDecimal.ZERO);
    }

    /**
     * 是否大于0
     *
     * @param number 数字
     * @return 大于0
     */
    public static boolean gtZero(Integer number) {
        int x = number == null ? 0 : number;
        return x > 0;
    }

    /**
     * Integer 转 int
     *
     * @param number 数字
     * @return int
     */
    public static int null2Zero(Integer number) {
        return number == null ? 0 : number;
    }

    /**
     * 构建sql in参数
     *
     * @param objectList 原始结果集
     * @return in 内的字符串
     */
    public static String buildForInSql(List<String> objectList) {
        if (CollUtil.isNotEmpty(objectList)) {
            StrBuilder strBuilder = StrBuilder.create();
            objectList.forEach(s -> strBuilder.append("'").append(s).append("',"));
            String s = strBuilder.toString();
            return StrUtil.removeSuffix(s, ",");
        }
        return null;
    }

    /**
     * 根据请求获取用户ip 取第一个非unknown的ip,穿透代理
     *
     * @param request 请求
     */
    public static String getIp(HttpServletRequest request) {
        String ip = null;
        for (String head : HEADERS_TO_TRY) {
            ip = request.getHeader(head);
            if (StrUtil.isNotEmpty(ip) && !UN_KNOWN.equalsIgnoreCase(ip)) {
                break;
            }
        }
        if (StrUtil.isBlank(ip)) {
            ip = request.getRemoteAddr();
        }
        //ip可能形如 117.1.1.1,192.168.0.01, 取第一个
        if (StrUtil.isBlank(ip) && ip.contains(StrUtil.COMMA)) {
            ip = ip.split(StrUtil.COMMA)[0];
        }
        //本机IP
        if (LOCALHOST_IP_LIST.contains(ip)) {
            //获取真正的本机内网IP
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                log.error(e.getMessage(), e);
            }
        }
        return ip;
    }

    /**
     * 将字符串中连续2个及以上的空格变为1个, 海关型号要求
     *
     * @param str 字符串
     * @return 转化后结果
     */
    public static String over2SpaceTo1(String str) {
        if (StrUtil.isBlank(str)) {
            return null;
        }
        return OVER_TWO_SPACE.matcher(str).replaceAll(" ");
    }

    /**
     * null 转空字符串,并去除前后空格
     *
     * @param s 原始字符串
     * @return 新字符串
     */
    public static String null2EmptyWithTrimNew(Object s) {
        return s != null && !"NULL".equalsIgnoreCase(s.toString()) ? s.toString().trim() : "";
    }

    /**
     * 获取当前请求对象
     *
     * @return 请求
     */
    public static HttpServletRequest currentRequest() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (sra == null) {
            return null;
        }
        return sra.getRequest();
    }
}
