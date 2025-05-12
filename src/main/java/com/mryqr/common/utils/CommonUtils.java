package com.mryqr.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.time.DayOfWeek.MONDAY;
import static java.time.LocalDate.of;
import static java.time.Year.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.regex.Pattern.matches;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;

public class CommonUtils {
    private static final String CGLIB_CLASS_SEPARATOR = "$$";

    public static String nullIfBlank(String string) {
        if (isBlank(string)) {
            return null;
        }

        return string;
    }

    public static String requireNonBlank(String str, String message) {
        if (isBlank(str)) {
            throw new IllegalArgumentException(message);
        }
        return str;
    }

    public static int currentSeason() {
        int currentMonth = LocalDate.now().getMonth().getValue();
        if (currentMonth <= 3) {
            return 1;
        }

        if (currentMonth <= 6) {
            return 2;
        }

        if (currentMonth <= 9) {
            return 3;
        }

        return 4;
    }

    public static Instant startOfCurrentWeek() {
        return LocalDate.now()
                .with(MONDAY)
                .atStartOfDay(systemDefault())
                .toInstant();
    }

    public static Instant startOfLastWeek() {
        return LocalDate.now()
                .minusWeeks(1)
                .with(MONDAY)
                .atStartOfDay(systemDefault())
                .toInstant();
    }

    public static Instant startOfCurrentMonth() {
        return LocalDate.now()
                .with(firstDayOfMonth())
                .atStartOfDay(systemDefault())
                .toInstant();
    }

    public static Instant startOfLastMonth() {
        return LocalDate.now()
                .minusMonths(1)
                .with(firstDayOfMonth())
                .atStartOfDay(systemDefault())
                .toInstant();
    }

    public static Instant startOfCurrentSeason() {
        int currentMonth = LocalDate.now().getMonth().getValue();
        int startMonth;
        if (currentMonth <= 3) {
            startMonth = 1;
        } else if (currentMonth <= 6) {
            startMonth = 4;
        } else if (currentMonth <= 9) {
            startMonth = 7;
        } else {
            startMonth = 10;
        }

        return of(now().getValue(), startMonth, 1)
                .atStartOfDay(systemDefault()).toInstant();

    }

    public static Instant startOfLastSeason() {
        int currentMonth = LocalDate.now().getMonth().getValue();
        if (currentMonth <= 3) {//去年的最后一个季度
            return of(now().getValue() - 1, 10, 1)
                    .atStartOfDay(systemDefault()).toInstant();
        }

        int startMonth;
        if (currentMonth <= 6) {
            startMonth = 1;
        } else if (currentMonth <= 9) {
            startMonth = 4;
        } else {
            startMonth = 7;
        }

        return of(now().getValue(), startMonth, 1)
                .atStartOfDay(systemDefault()).toInstant();

    }

    public static Instant startOfCurrentYear() {
        return LocalDate.now()
                .with(firstDayOfYear())
                .atStartOfDay(systemDefault())
                .toInstant();
    }

    public static Instant startOfLastYear() {
        return LocalDate.now()
                .minusYears(1)
                .with(firstDayOfYear())
                .atStartOfDay(systemDefault())
                .toInstant();
    }

    public static boolean isValidUrl(String urlString) {
        if (isBlank(urlString)) {
            return false;
        }

        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static boolean isMobileNumber(String value) {
        return matches(MryRegexConstants.MOBILE_PATTERN, value);
    }

    public static boolean isEmail(String value) {
        return matches(MryRegexConstants.EMAIL_PATTERN, value);
    }

    public static String maskMobileOrEmail(String mobileOrEmail) {
        if (isBlank(mobileOrEmail)) {
            return mobileOrEmail;
        }

        if (isMobileNumber(mobileOrEmail)) {
            return mobileOrEmail.replaceAll("(\\w{3})\\w*(\\w{4})", "$1****$2");
        }

        return mobileOrEmail.replaceAll("(^[^@]{3}|(?!^)\\G)[^@]", "$1*");
    }

    public static String maskMobile(String mobile) {
        if (isBlank(mobile)) {
            return mobile;
        }

        return mobile.replaceAll("(\\w{3})\\w*(\\w{4})", "$1****$2");
    }

    //有些immutable集合做contains操作时对null元素抛出NPE，很烦人，这里提前做个判断
    public static <T> boolean contains(Collection<T> collection, T element) {
        if (collection == null || element == null) {
            return false;
        }

        return collection.contains(element);
    }

    public static List<Double> splitAndSortNumberSegment(String str) {
        if (isBlank(str)) {
            return List.of();
        }

        return Arrays.stream(split(str, ","))
                .map(subStr -> asList(split(subStr, "，")))
                .flatMap(Collection::stream)
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .filter(NumberUtils::isCreatable)
                .map(Double::valueOf)
                .distinct()
                .sorted()
                .limit(20)//最大允许20个范围区间
                .collect(toImmutableList());
    }

    public static String[] splitSearchBySpace(String search) {
        if (isBlank(search)) {
            return new String[0];
        }

        String[] splitted = search.trim().split("\\s+");
        return splitted.length > 3 ? copyOfRange(splitted, 0, 3) : splitted;
    }


    public static Class<?> singleParameterizedArgumentClassOf(Class<?> aClass) {
        // The aClass might be proxied by Spring CGlib, so we need to get the real targeted class
        Class<?> realClass = aClass.getName().contains(CGLIB_CLASS_SEPARATOR) ? aClass.getSuperclass() : aClass;

        Type genericSuperclass = realClass.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType)) {
            return null;
        }

        Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();

        if (actualTypeArguments.length != 1) {
            throw new RuntimeException("Expecting exactly one parameterized type argument for " + realClass);
        }

        Type actualTypeArgument = actualTypeArguments[0];
        if (actualTypeArgument instanceof Class) {
            return (Class<?>) actualTypeArgument;
        }
        return null;
    }

}
