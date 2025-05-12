package com.mryqr.core.app.domain.ui;

import com.mryqr.common.validation.color.Color;
import com.mryqr.core.app.domain.ui.border.Border;
import com.mryqr.core.app.domain.ui.shadow.Shadow;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.common.utils.MryConstants.*;
import static com.mryqr.core.app.domain.ui.border.Border.noBorder;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class AppearanceStyle {
    @Valid
    @NotNull
    private final Border border;//边框

    @Valid
    @NotNull
    private final Shadow shadow;//阴影

    @Min(MIN_BORDER_RADIUS)
    @Max(MAX_BORDER_RADIUS)
    private final int borderRadius;//圆角半径

    @Min(MIN_PADDING)
    @Max(MAX_PADDING)
    private final int vPadding;//垂直内边距

    @Min(MIN_PADDING)
    @Max(MAX_PADDING)
    private final int hPadding;//水平内边距

    @Color
    private final String backgroundColor;//背景色

    public static AppearanceStyle defaultAppearanceStyle() {
        return AppearanceStyle.builder()
                .backgroundColor("rgba(255, 255, 255, 1)")
                .borderRadius(0)
                .shadow(Shadow.builder()
                        .width(6)
                        .color("rgba(0, 0, 0, 0.15)")
                        .build())
                .border(noBorder())
                .vPadding(0)
                .hPadding(15)
                .build();
    }
}
