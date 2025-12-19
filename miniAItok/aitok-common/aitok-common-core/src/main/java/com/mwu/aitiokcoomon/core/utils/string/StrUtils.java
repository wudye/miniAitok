
        package com.mwu.aitiokcoomon.core.utils.string;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 */
public class StrUtils {

    /**
     * 返回长度不超过 maxLength 的字符串，超出时加省略号 "..."
     *
     * @param str       源字符串/字符序列
     * @param maxLength 最大长度（包含省略号长度）
     * @return 处理后的字符串
     */
    public static String maxLength(CharSequence str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (maxLength <= 0) {
            return "";
        }
        String s = str.toString();
        if (s.length() <= maxLength) {
            return s;
        }
        if (maxLength <= 3) {
            return s.substring(0, maxLength);
        }
        return s.substring(0, maxLength - 3) + "...";
    }

    /**
     * 给定字符串是否以集合中任何一个字符串开始
     *
     * @param str      给定字符串
     * @param prefixes 需要检测的开始字符串集合
     */
    public static boolean startWithAny(String str, Collection<String> prefixes) {
        if (StringUtils.isEmpty(str) || prefixes == null || prefixes.isEmpty()) {
            return false;
        }
        for (String prefix : prefixes) {
            if (StringUtils.startsWith(str, prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将字符串按指定分隔符拆分并转换为 Long 列表（忽略空项）
     */
    public static List<Long> splitToLong(String value, CharSequence separator) {
        if (StringUtils.isEmpty(value)) {
            return Arrays.asList();
        }
        String sep = separator == null ? "," : separator.toString();
        Pattern p = Pattern.compile(Pattern.quote(sep));
        return Arrays.stream(p.split(value))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public static Set<Long> splitToLongSet(String value) {
        return splitToLongSet(value, ",");
    }

    public static Set<Long> splitToLongSet(String value, CharSequence separator) {
        return splitToLong(value, separator).stream().collect(Collectors.toSet());
    }

    /**
     * 将字符串按指定分隔符拆分并转换为 Integer 列表（忽略空项）
     */
    public static List<Integer> splitToInteger(String value, CharSequence separator) {
        if (StringUtils.isEmpty(value)) {
            return Arrays.asList();
        }
        String sep = separator == null ? "," : separator.toString();
        Pattern p = Pattern.compile(Pattern.quote(sep));
        return Arrays.stream(p.split(value))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    /**
     * 移除字符串中包含指定 sequence 的行
     */
    public static String removeLineContains(String content, String sequence) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(sequence)) {
            return content;
        }
        return Arrays.stream(content.split("\n"))
                .filter(line -> !line.contains(sequence))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 拼接方法的参数
     * 特殊：排除一些无法序列化的参数，如 ServletRequest、ServletResponse、MultipartFile
     */
    public static String joinMethodArgs(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args)
                .map(item -> {
                    if (item == null) {
                        return "";
                    }
                    String clazzName = item.getClass().getName();
                    if (StringUtils.startsWithAny(clazzName, "javax.servlet", "jakarta.servlet", "org.springframework.web")) {
                        return "";
                    }
                    return String.valueOf(item);
                })
                .collect(Collectors.joining(","));
    }

}
