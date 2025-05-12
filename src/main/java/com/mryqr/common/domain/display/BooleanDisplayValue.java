package com.mryqr.common.domain.display;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class BooleanDisplayValue extends DisplayValue {
    private boolean yes;

    public BooleanDisplayValue(String key, boolean yes) {
        super(key, DisplayValueType.BOOLEAN_DISPLAY_VALUE);
        this.yes = yes;
    }
}
