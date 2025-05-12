package com.mryqr.core.app.domain.ui.shadow;

import com.mryqr.common.validation.color.Color;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Shadow {
    @Min(0)
    @Max(100)
    private final int width;//边框宽度

    @Color
    private final String color;//阴影颜色

    public static Shadow noShadow() {
        return Shadow.builder().width(0).color("rgba(0, 0, 0, 0.1)").build();
    }
}
