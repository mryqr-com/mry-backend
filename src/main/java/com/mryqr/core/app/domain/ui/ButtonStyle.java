package com.mryqr.core.app.domain.ui;

import com.mryqr.common.validation.color.Color;
import com.mryqr.core.app.domain.ui.border.Border;
import com.mryqr.core.app.domain.ui.border.BorderSide;
import com.mryqr.core.app.domain.ui.shadow.Shadow;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static com.mryqr.common.utils.MryConstants.MAX_PADDING;
import static com.mryqr.common.utils.MryConstants.MIN_PADDING;
import static com.mryqr.core.app.domain.ui.border.BorderType.SOLID;
import static com.mryqr.core.app.domain.ui.shadow.Shadow.noShadow;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ButtonStyle {
    @Valid
    @NotNull
    private final FontStyle fontStyle;//字体类型

    @Color
    private final String backgroundColor;//按钮背景颜色

    @Valid
    @NotNull
    private final Border border;//边框

    @Valid
    @NotNull
    private final Shadow shadow;//边框阴影

    @Min(MIN_PADDING)
    @Max(MAX_PADDING)
    private final int vPadding;//垂直内边距

    public static ButtonStyle defaultButtonStyle() {
        return ButtonStyle.builder()
                .fontStyle(FontStyle.builder()
                        .fontFamily("默认")
                        .fontSize(14)
                        .bold(false)
                        .italic(false)
                        .color("rgba(48, 49, 51, 1)")
                        .build())
                .backgroundColor("rgba(237, 241, 248, 1)")
                .border(Border.builder().type(SOLID).width(1).sides(Set.of(BorderSide.values())).color("rgba(220, 223, 230, 1)").build())
                .shadow(noShadow())
                .vPadding(10)
                .build();
    }

}
