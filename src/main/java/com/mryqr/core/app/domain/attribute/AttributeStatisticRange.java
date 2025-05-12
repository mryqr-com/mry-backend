package com.mryqr.core.app.domain.attribute;

import java.time.Instant;
import java.util.Optional;

import static com.mryqr.common.utils.CommonUtils.*;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public enum AttributeStatisticRange {
    NO_LIMIT,
    THIS_WEEK,
    THIS_MONTH,
    THIS_SEASON,
    THIS_YEAR;

    public static Optional<Instant> startAt(AttributeStatisticRange range) {
        switch (range) {
            case THIS_WEEK -> {
                return ofNullable(startOfCurrentWeek());
            }
            case THIS_MONTH -> {
                return ofNullable(startOfCurrentMonth());
            }
            case THIS_SEASON -> {
                return ofNullable(startOfCurrentSeason());
            }
            case THIS_YEAR -> {
                return ofNullable(startOfCurrentYear());
            }
        }
        return empty();

    }
}
