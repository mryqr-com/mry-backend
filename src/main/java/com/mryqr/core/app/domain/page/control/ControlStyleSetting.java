package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.ui.border.Border;
import com.mryqr.core.app.domain.ui.borderradius.BorderRadius;
import com.mryqr.core.app.domain.ui.shadow.Shadow;
import com.mryqr.core.common.validation.color.Color;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.app.domain.ui.border.Border.noBorder;
import static com.mryqr.core.app.domain.ui.borderradius.BorderRadius.noBorderRadius;
import static com.mryqr.core.app.domain.ui.shadow.Shadow.noShadow;
import static com.mryqr.core.common.utils.MryConstants.MAX_MARGIN;
import static com.mryqr.core.common.utils.MryConstants.MAX_PADDING;
import static com.mryqr.core.common.utils.MryConstants.MIN_MARGIN;
import static com.mryqr.core.common.utils.MryConstants.MIN_PADDING;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ControlStyleSetting {
    @Min(MIN_MARGIN)
    @Max(MAX_MARGIN)
    private int topMargin;//顶部外边距

    @Min(MIN_MARGIN)
    @Max(MAX_MARGIN)
    private int bottomMargin;//底部外边距

    @Valid
    @NotNull
    private final Border border;//边框

    @Valid
    @NotNull
    private final Shadow shadow;//阴影

    @Valid
    @NotNull
    private final BorderRadius borderRadius;

    @Min(MIN_PADDING)
    @Max(MAX_PADDING)
    private final int vPadding;//垂直内边距

    @Min(MIN_PADDING)
    @Max(MAX_PADDING)
    private final int hPadding;//水平内边距

    @Color
    private final String backgroundColor;//背景色

    public static ControlStyleSetting defaultControlStyleSetting() {
        return ControlStyleSetting.builder()
                .topMargin(25)
                .bottomMargin(25)
                .backgroundColor(null)
                .borderRadius(noBorderRadius())
                .shadow(noShadow())
                .border(noBorder())
                .vPadding(0)
                .hPadding(0)
                .build();
    }

}
