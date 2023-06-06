package com.mryqr.core.common.domain.display;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.mryqr.core.common.domain.display.DisplayValueType.POINT_CHECK_DISPLAY_VALUE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PointCheckDisplayValue extends DisplayValue {
    private boolean pass;

    public PointCheckDisplayValue(String key, boolean pass) {
        super(key, POINT_CHECK_DISPLAY_VALUE);
        this.pass = pass;
    }
}
