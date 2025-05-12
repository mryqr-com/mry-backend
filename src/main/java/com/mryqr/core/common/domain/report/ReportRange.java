package com.mryqr.core.common.domain.report;

import java.util.Optional;

import static com.mryqr.core.common.utils.CommonUtils.startOfCurrentMonth;
import static com.mryqr.core.common.utils.CommonUtils.startOfCurrentSeason;
import static com.mryqr.core.common.utils.CommonUtils.startOfCurrentWeek;
import static com.mryqr.core.common.utils.CommonUtils.startOfCurrentYear;
import static com.mryqr.core.common.utils.CommonUtils.startOfLastMonth;
import static com.mryqr.core.common.utils.CommonUtils.startOfLastSeason;
import static com.mryqr.core.common.utils.CommonUtils.startOfLastWeek;
import static com.mryqr.core.common.utils.CommonUtils.startOfLastYear;
import static java.time.Instant.now;
import static java.time.LocalDate.ofInstant;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Optional.empty;

public enum ReportRange {
    NO_LIMIT,//无限制
    THIS_WEEK,//本周，从周一开始
    THIS_MONTH,//本月
    THIS_SEASON,//本季度
    THIS_YEAR,//本年
    LAST_WEEK,//上周
    LAST_MONTH,//上月
    LAST_SEASON,//上季度
    LAST_YEAR,//上年
    LAST_7_DAYS,//最近7天
    LAST_30_DAYS,//最近30天
    LAST_90_DAYS,//最近90天
    LAST_HALF_YEAR,//最近半年
    LAST_ONE_YEAR;//最近一年

    public static Optional<TimeRange> timeRangeOf(ReportRange range) {
        switch (range) {
            case THIS_WEEK -> {
                return Optional.of(TimeRange.of(startOfCurrentWeek(), now()));
            }
            case THIS_MONTH -> {
                return Optional.of(TimeRange.of(startOfCurrentMonth(), now()));
            }
            case THIS_SEASON -> {
                return Optional.of(TimeRange.of(startOfCurrentSeason(), now()));
            }
            case THIS_YEAR -> {
                return Optional.of(TimeRange.of(startOfCurrentYear(), now()));
            }
            case LAST_WEEK -> {
                return Optional.of(TimeRange.of(startOfLastWeek(), startOfCurrentWeek()));
            }
            case LAST_MONTH -> {
                return Optional.of(TimeRange.of(startOfLastMonth(), startOfCurrentMonth()));
            }
            case LAST_SEASON -> {
                return Optional.of(TimeRange.of(startOfLastSeason(), startOfCurrentSeason()));
            }
            case LAST_YEAR -> {
                return Optional.of(TimeRange.of(startOfLastYear(), startOfCurrentYear()));
            }
            case LAST_7_DAYS -> {
                return Optional.of(TimeRange.of(ofInstant(now()
                                .minus(6, DAYS), systemDefault())
                                .atStartOfDay(systemDefault())
                                .toInstant(),
                        now()));
            }
            case LAST_30_DAYS -> {
                return Optional.of(TimeRange.of(ofInstant(now()
                                .minus(29, DAYS), systemDefault())
                                .atStartOfDay(systemDefault())
                                .toInstant(),
                        now()));
            }
            case LAST_90_DAYS -> {
                return Optional.of(TimeRange.of(ofInstant(now()
                                .minus(89, DAYS), systemDefault())
                                .atStartOfDay(systemDefault())
                                .toInstant(),
                        now()));
            }
            case LAST_HALF_YEAR -> {
                return Optional.of(TimeRange.of(ofInstant(now()
                                .minus(182, DAYS), systemDefault())
                                .atStartOfDay(systemDefault())
                                .toInstant(),
                        now()));
            }
            case LAST_ONE_YEAR -> {
                return Optional.of(TimeRange.of(ofInstant(now()
                                .minus(364, DAYS), systemDefault())
                                .atStartOfDay(systemDefault())
                                .toInstant(),
                        now()));
            }
            default -> {
                return empty();
            }
        }
    }

}
