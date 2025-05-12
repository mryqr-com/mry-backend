package com.mryqr.core.app.domain.page.setting;

import java.time.Instant;

import static java.time.Instant.now;

//允许提交者修改时的限制设置
public enum SubmitterUpdateRange {
    IN_HALF_HOUR(30 * 60 * 1000),
    IN_1_HOUR(60 * 60 * 1000),
    IN_4_HOUR(4 * 60 * 60 * 1000),
    IN_12_HOUR(12 * 60 * 60 * 1000),
    IN_1_DAY(24 * 60 * 60 * 1000),
    IN_2_DAY(48 * 60 * 60 * 1000),
    IN_1_WEEK(7 * 24 * 60 * 60 * 1000),
    IN_1_MONTH(30 * 24 * 60 * 60 * 1000L),
    IN_3_MONTH(3 * 30 * 24 * 60 * 60 * 1000L),
    IN_6_MONTH(6 * 30 * 24 * 60 * 60 * 1000L),
    IN_1_YEAR(12 * 30 * 24 * 60 * 60 * 1000L),
    NO_RESTRICTION(0);

    private final long durationMilliseconds;

    SubmitterUpdateRange(long durationMilliseconds) {
        this.durationMilliseconds = durationMilliseconds;
    }

    public boolean validFrom(Instant instant) {
        if (this == NO_RESTRICTION) {
            return true;
        }

        return instant.toEpochMilli() + durationMilliseconds > now().toEpochMilli();
    }

}
