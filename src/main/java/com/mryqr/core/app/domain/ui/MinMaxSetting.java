package com.mryqr.core.app.domain.ui;

import com.mryqr.core.common.exception.MryException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.common.exception.ErrorCode.MIN_GREATER_THAN_MAX;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MinMaxSetting {
    private final double min;
    private final double max;

    public void validate() {
        if (min > max) {
            throw new MryException(MIN_GREATER_THAN_MAX, "最小值不能大于最大值。");
        }
    }

    public static MinMaxSetting minMaxOf(double min, double max) {
        return MinMaxSetting.builder().min(min).max(max).build();
    }
}
