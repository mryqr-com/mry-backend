package com.mryqr.core.common.domain.display;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.mryqr.core.common.domain.display.DisplayValueType.NUMBER_DISPLAY_VALUE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class NumberDisplayValue extends DisplayValue {
    private Double number;

    public NumberDisplayValue(String key, Double number) {
        super(key, NUMBER_DISPLAY_VALUE);
        this.number = number;
    }

}
