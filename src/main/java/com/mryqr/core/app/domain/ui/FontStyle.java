package com.mryqr.core.app.domain.ui;


import com.mryqr.common.validation.color.Color;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class FontStyle {
    @NotBlank
    @Size(max = 20)
    private final String fontFamily;//字体

    @Min(5)
    @Max(80)
    private final int fontSize;//字号

    private final boolean bold;//是否粗体

    private final boolean italic;//是否斜体

    @Color
    private final String color;//字体颜色

    public static FontStyle defaultFontStyle() {
        return FontStyle.builder()
                .fontFamily("默认")
                .fontSize(14)
                .bold(false)
                .italic(false)
                .color("#606266")
                .build();
    }

}
