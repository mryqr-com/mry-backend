package com.mryqr.core.app.domain;

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

import java.util.Set;

import static com.mryqr.common.utils.MryConstants.MAX_PADDING;
import static com.mryqr.common.utils.MryConstants.MIN_PADDING;
import static com.mryqr.core.app.domain.ui.border.BorderSide.BOTTOM;
import static com.mryqr.core.app.domain.ui.border.BorderType.SOLID;
import static com.mryqr.core.app.domain.ui.shadow.Shadow.noShadow;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class AppTopBar {
    @Min(30)
    @Max(80)
    private final int height;//高度

    @Color
    private final String textColor;//文本颜色

    @Color
    private final String backgroundColor;//背景颜色

    @Valid
    @NotNull
    private final Border border;//边框

    @Valid
    @NotNull
    private Shadow shadow;//阴影

    @Min(MIN_PADDING)
    @Max(MAX_PADDING)
    private final int hPadding;//水平内边距

    public static AppTopBar defaultAppTopBar() {
        return AppTopBar.builder()
                .height(50)
                .textColor("#484a4d")
                .backgroundColor("rgba(255, 255, 255, 1)")
                .border(Border.builder().type(SOLID).width(1).sides(Set.of(BOTTOM)).color("#DCDFE6").build())
                .shadow(noShadow())
                .hPadding(24)
                .build();
    }
}
