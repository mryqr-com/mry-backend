package com.mryqr.core.common.domain.display;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.mryqr.core.common.domain.display.DisplayValueType.TEXT_OPTION_DISPLAY_VALUE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class TextOptionDisplayValue extends DisplayValue {
    private String optionId;

    public TextOptionDisplayValue(String key, String optionId) {
        super(key, TEXT_OPTION_DISPLAY_VALUE);
        this.optionId = optionId;
    }
}
