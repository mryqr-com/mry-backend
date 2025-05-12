package com.mryqr.core.app.domain.ui.border;

import com.mryqr.core.common.validation.collection.NoNullElement;
import com.mryqr.core.common.validation.color.Color;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static com.mryqr.core.app.domain.ui.border.BorderType.NONE;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Border {
    @NotNull
    private final BorderType type;//边框类型

    @Min(0)
    @Max(100)
    private final int width;//宽度

    @NotNull
    @NoNullElement
    private final Set<BorderSide> sides;//四边范围

    @Color
    private final String color;//颜色

    public static Border noBorder() {
        return Border.builder().type(NONE).width(1).sides(Set.of(BorderSide.values())).color("rgba(220, 223, 230, 1)").build();
    }

}
