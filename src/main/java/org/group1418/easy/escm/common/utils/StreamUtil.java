package org.group1418.easy.escm.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import org.group1418.easy.escm.common.wrapper.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author yq 2023/7/17 17:22
 * @description StreamUtil stream工具类
 */
public class StreamUtil {

    /**
     * T集合转MAP,多个只取其一T
     *
     * @param from    原始结合
     * @param keyFunc key函数
     * @param <T>     原始类型
     * @param <K>     key类型
     * @return map
     */
    public static <T, K> Map<K, T> toMap(Collection<T> from, Function<T, K> keyFunc) {
        if (CollUtil.isEmpty(from)) {
            return MapUtil.empty();
        }
        return from.stream().collect(Collectors.toMap(keyFunc, Function.identity(), (a, b) -> a));
    }

    /**
     * T集合转Map,多个只取其一 T的属性
     *
     * @param from      原始集合
     * @param keyFunc   key函数
     * @param valueFunc value函数
     * @param <T>       原始类型
     * @param <K>       key类型
     * @param <V>       value类型
     * @return map
     */
    public static <T, K, V> Map<K, V> toMap(Collection<T> from, Function<T, K> keyFunc, Function<T, V> valueFunc) {
        if (CollUtil.isEmpty(from)) {
            return MapUtil.empty();
        }
        return from.stream().collect(Collectors.toMap(keyFunc, valueFunc, (a, b) -> a));
    }

    /**
     * T集合转Map,按指定规则取其一 T
     *
     * @param from          原始集合
     * @param keyFunc       key函数
     * @param mergeFunction 合并函数
     * @param <T>           原始类型
     * @param <K>           key类型
     * @return map
     */
    public static <T, K> Map<K, T> toMap(Collection<T> from, Function<T, K> keyFunc, BinaryOperator<T> mergeFunction) {
        if (CollUtil.isEmpty(from)) {
            return MapUtil.empty();
        }
        return from.stream().collect(Collectors.toMap(keyFunc, Function.identity(), mergeFunction));
    }

    /**
     * T集合按指定key按指定取值函数合并成新集合
     *
     * @param from          原始集合
     * @param keyFunc       key函数
     * @param mergeFunction 合并函数
     * @param <T>           原始类型
     * @param <K>           key类型
     * @return map
     */
    public static <T, K> List<T> merge(Collection<T> from, Function<T, K> keyFunc, BinaryOperator<T> mergeFunction) {
        if (CollUtil.isEmpty(from)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(toMap(from, keyFunc, mergeFunction).values());
    }

    /**
     * T集合按指定标识分组
     *
     * @param from       原始集合
     * @param classifier 标识函数
     * @param <T>        原始类型
     * @param <K>        key类型
     * @return map
     */
    public static <T, K> Map<K, List<T>> groupingBy(Collection<T> from, Function<T, K> classifier) {
        if (CollUtil.isEmpty(from)) {
            return MapUtil.empty();
        }
        return from.stream().collect(Collectors.groupingBy(classifier));
    }

    /**
     * T集合按指定条件过滤并标识分组
     *
     * @param from       原始结婚
     * @param predicate  过滤条件
     * @param classifier 标识函数
     * @param <T>        原始类型
     * @param <K>        key类型
     * @return map
     */
    public static <T, K> Map<K, List<T>> groupingBy(Collection<T> from, Predicate<T> predicate, Function<T, K> classifier) {
        if (CollUtil.isEmpty(from)) {
            return MapUtil.empty();
        }
        return from.stream().filter(predicate).collect(Collectors.groupingBy(classifier));
    }

    /**
     * 指定集合取指定value去重
     *
     * @param from      原始集合
     * @param valueFunc value函数
     * @param <T>       原始类型
     * @param <V>       value类型
     * @return list
     */
    public static <T, V> List<V> distinct(Collection<T> from, Function<T, V> valueFunc) {
        if (CollUtil.isEmpty(from)) {
            return new ArrayList<>();
        }
        return from.stream().map(valueFunc).distinct().collect(Collectors.toList());
    }

    /**
     * 指定集合取指定value去重
     *
     * @param from      原始集合
     * @param valueFunc value函数
     * @param <T>       原始类型
     * @param <V>       value类型
     * @return set
     */
    public static <T, V> Set<V> distinctSet(Collection<T> from, Function<T, V> valueFunc) {
        if (CollUtil.isEmpty(from)) {
            return new HashSet<>();
        }
        return from.stream().map(valueFunc).collect(Collectors.toSet());
    }

    /**
     * 合计
     *
     * @param from        原始集合
     * @param valueFunc   BigDecimal取值
     * @param accumulator 计算方式
     * @param <T>         原始类型
     * @return 合计值
     */
    public static <T> BigDecimal reduceBigDecimal(Collection<T> from, Function<T, BigDecimal> valueFunc,
                                                  BinaryOperator<BigDecimal> accumulator) {
        return from.stream().map(t -> NumberUtil.nullToZero(valueFunc.apply(t)))
                .reduce(BigDecimal.ZERO, accumulator);
    }

    /**
     * 从指定集合查找第一个符合条件项
     *
     * @param from      原始集合
     * @param predicate 条件
     * @param <T>       原始类型
     * @return 第一个匹配值
     */
    public static <T> T findFirst(Collection<T> from, Predicate<T> predicate) {
        if (CollUtil.isEmpty(from)) {
            return null;
        }
        return from.stream().filter(predicate).findFirst().orElse(null);
    }

    /**
     * 从指定集合查找第一个符合条件项的指定属性
     *
     * @param from      原始集合
     * @param predicate 条件
     * @param func value函数
     * @param <T>       原始类型
     * @return 第一个匹配值
     */
    public static <T,R> R findFirst(Collection<T> from, Predicate<T> predicate, Function<? super T, ? extends R> func) {
        if (CollUtil.isEmpty(from)) {
            return null;
        }
        return from.stream().filter(predicate).findFirst().map(func).orElse(null);
    }

    /**
     * 分组执行function并合并结果集
     *
     * @param allQ     原始结婚
     * @param function 拆分后执行的函数
     * @param loopSize 单组最大数量
     * @param <R>      返回值
     * @param <T>      原始值
     * @return 结果集
     */
    public static <R, T> List<R> splitDoAndUnionResult(List<T> allQ, Function<List<T>, List<R>> function, Integer loopSize) {
        int loopSizeLimit = loopSize != null ? loopSize : 1000;
        if (CollUtil.isEmpty(allQ)) {
            return null;
        }
        int size = allQ.size();
        if (size > loopSizeLimit) {
            //按大小拆分
            List<List<T>> qList = CollUtil.split(allQ, loopSizeLimit);
            return qList.stream().flatMap(qs -> function.apply(qs).stream()).collect(Collectors.toList());
        } else {
            return function.apply(allQ);
        }
    }

    /**
     * 合并集合
     *
     * @param c1     集合
     * @param c2     开合
     * @param mapper 去重function
     * @param <T>    原始类型
     * @param <R>    去重属性类型
     * @return 去重后的集合
     */
    public static <T, R> List<T> unionDistinct(List<T> c1, List<T> c2, Function<? super T, ? extends R> mapper) {
        if (CollUtil.isEmpty(c1)) {
            return c2;
        }
        if (CollUtil.isEmpty(c2)) {
            return c1;
        }
        return CollUtil.unionAll(c1, c2).stream().filter(distinctByKey(mapper)).collect(Collectors.toList());
    }

    /**
     * 按指定key去重predicate
     * @param keyExtractor key获取函数
     * @param <T> 原始类型
     * @return predicate
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


    /**
     * 从数组中找出第一个满足条件的对象
     *
     * @param array     数组
     * @param predicate 条件
     * @param <T>       集合泛型
     * @return 对象
     */
    public static <T> T findFirst(T[] array, Predicate<? super T> predicate) {
        if (ArrayUtil.isEmpty(array)) {
            return null;
        }
        Optional<T> optionalConfig = Arrays.stream(array).filter(predicate).findFirst();
        return optionalConfig.orElse(null);
    }
}
