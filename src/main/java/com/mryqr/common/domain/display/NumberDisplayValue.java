package com.mryqr.common.domain.display;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class NumberDisplayValue extends DisplayValue {
    private Double number;

    public NumberDisplayValue(String key, Double number) {
        super(key, DisplayValueType.NUMBER_DISPLAY_VALUE);
        this.number = number;
    }

}
