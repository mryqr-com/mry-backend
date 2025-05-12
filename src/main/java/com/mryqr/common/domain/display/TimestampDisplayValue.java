package com.mryqr.common.domain.display;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static com.mryqr.common.domain.display.DisplayValueType.TIMESTAMP_DISPLAY_VALUE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class TimestampDisplayValue extends DisplayValue {
    private Instant timestamp;

    public TimestampDisplayValue(String key, Instant timestamp) {
        super(key, TIMESTAMP_DISPLAY_VALUE);
        this.timestamp = timestamp;
    }
}
