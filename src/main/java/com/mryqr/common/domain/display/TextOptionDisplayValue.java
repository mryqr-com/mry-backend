package com.mryqr.common.domain.display;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class TextOptionDisplayValue extends DisplayValue {
    private String optionId;

    public TextOptionDisplayValue(String key, String optionId) {
        super(key, DisplayValueType.TEXT_OPTION_DISPLAY_VALUE);
        this.optionId = optionId;
    }
}
