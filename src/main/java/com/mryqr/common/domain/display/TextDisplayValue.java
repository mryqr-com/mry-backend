package com.mryqr.common.domain.display;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.mryqr.common.domain.display.DisplayValueType.TEXT_DISPLAY_VALUE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class TextDisplayValue extends DisplayValue {
    private String text;

    public TextDisplayValue(String key, String text) {
        super(key, TEXT_DISPLAY_VALUE);
        this.text = text;
    }
}
