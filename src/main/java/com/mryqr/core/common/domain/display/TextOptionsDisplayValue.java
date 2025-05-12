package com.mryqr.core.common.domain.display;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.mryqr.core.common.domain.display.DisplayValueType.TEXT_OPTIONS_DISPLAY_VALUE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class TextOptionsDisplayValue extends DisplayValue {
    private List<String> optionIds;

    public TextOptionsDisplayValue(String key, List<String> optionIds) {
        super(key, TEXT_OPTIONS_DISPLAY_VALUE);
        this.optionIds = optionIds;
    }
}
