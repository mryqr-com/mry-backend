package com.mryqr.core.app.domain.ui;

import com.mryqr.core.app.domain.ui.align.HorizontalAlignType;
import com.mryqr.core.app.domain.ui.border.Border;
import com.mryqr.core.app.domain.ui.shadow.Shadow;
import com.mryqr.core.common.validation.color.Color;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.app.domain.ui.FontStyle.defaultFontStyle;
import static com.mryqr.core.app.domain.ui.align.HorizontalAlignType.JUSTIFY;
import static com.mryqr.core.app.domain.ui.border.Border.noBorder;
import static com.mryqr.core.app.domain.ui.shadow.Shadow.noShadow;
import static com.mryqr.core.common.utils.MryConstants.MAX_BORDER_RADIUS;
import static com.mryqr.core.common.utils.MryConstants.MAX_MARGIN;
import static com.mryqr.core.common.utils.MryConstants.MAX_PADDING;
import static com.mryqr.core.common.utils.MryConstants.MIN_BORDER_RADIUS;
import static com.mryqr.core.common.utils.MryConstants.MIN_MARGIN;
import static com.mryqr.core.common.utils.MryConstants.MIN_PADDING;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class BoxedTextStyle {
    @Valid
    @NotNull
    private final FontStyle fontStyle;//字体类型

    @NotNull
    private final HorizontalAlignType alignType;//字体居中类型

    @Color
    private final String backgroundColor;//文本背景颜色

    @Min(1)
    @Max(2)
    private final float lineHeight;//行高

    private final boolean fullWidth;//是否全宽度

    @Valid
    @NotNull
    private final Border border;//边框

    @Valid
    @NotNull
    private final Shadow shadow;//边框阴影

    @Min(MIN_PADDING)
    @Max(MAX_PADDING)
    private final int vPadding;//垂直内边距

    @Min(MIN_PADDING)
    @Max(MAX_PADDING)
    private final int hPadding;//水平内边距

    @Min(MIN_MARGIN)
    @Max(MAX_MARGIN)
    private final int topMargin;//顶部外边距

    @Min(MIN_MARGIN)
    @Max(MAX_MARGIN)
    private final int bottomMargin;//底部外边距

    @Min(MIN_BORDER_RADIUS)
    @Max(MAX_BORDER_RADIUS)
    private final int borderRadius;//圆角半径

    public static BoxedTextStyle defaultBoxedTextStyle() {
        return defaultBoxedTextStyleBuilder().build();
    }

    public static BoxedTextStyle defaultControlDescriptionStyle() {
        return defaultBoxedTextStyleBuilder()
                .fontStyle(FontStyle.builder()
                        .fontFamily("默认")
                        .fontSize(14)
                        .bold(false)
                        .italic(false)
                        .color("#909399")
                        .build())
                .bottomMargin(5)
                .build();
    }

    private static BoxedTextStyleBuilder defaultBoxedTextStyleBuilder() {
        return BoxedTextStyle.builder()
                .fontStyle(defaultFontStyle())
                .alignType(JUSTIFY)
                .lineHeight(1.4f)
                .fullWidth(true)
                .backgroundColor(null)
                .border(noBorder())
                .shadow(noShadow())
                .vPadding(0)
                .hPadding(0)
                .topMargin(0)
                .bottomMargin(5)
                .borderRadius(0);
    }

}
