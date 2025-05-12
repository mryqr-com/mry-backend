package com.mryqr.core.app.domain.page.header;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.core.app.domain.ui.BoxedTextStyle;
import com.mryqr.core.app.domain.ui.FontStyle;
import com.mryqr.core.app.domain.ui.ImageCropType;
import com.mryqr.core.app.domain.ui.align.VerticalAlignType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.mryqr.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static com.mryqr.core.app.domain.page.header.PageHeaderType.CUSTOM;
import static com.mryqr.core.app.domain.ui.ImageCropType.FOUR_TO_THREE;
import static com.mryqr.core.app.domain.ui.align.HorizontalAlignType.JUSTIFY;
import static com.mryqr.core.app.domain.ui.align.VerticalAlignType.MIDDLE;
import static com.mryqr.core.app.domain.ui.border.Border.noBorder;
import static com.mryqr.core.app.domain.ui.shadow.Shadow.noShadow;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class PageHeader {
    @NotNull
    private final PageHeaderType type;//类型

    private final boolean showImage;//是否显示图片

    @Valid
    private final UploadedFile image;//页眉图片

    @NotNull
    private final ImageCropType imageCropType;//图片裁剪类型

    private final boolean showText;//是否显示抬头

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String text;//抬头文本

    @Valid
    @NotNull
    private final BoxedTextStyle textStyle;//抬头文本样式

    @Valid
    private final UploadedFile logoImage;//抬头logo

    @Min(10)
    @Max(100)
    private final int logoHeight;//抬头logo的的高度

    @NotNull
    private final VerticalAlignType logoAlign;//抬头logo垂直对齐方式

    public static PageHeader defaultPageHeader() {
        return defaultPageHeaderBuilder().build();
    }

    public static PageHeaderBuilder defaultPageHeaderBuilder() {
        return PageHeader.builder()
                .type(CUSTOM)
                .showImage(true)
                .image(null)
                .imageCropType(FOUR_TO_THREE)
                .showText(false)
                .text("抬头文本")
                .textStyle(BoxedTextStyle.builder()
                        .fontStyle(FontStyle.builder()
                                .fontFamily("默认")
                                .fontSize(14)
                                .bold(false)
                                .italic(false)
                                .color("#444")
                                .build())
                        .alignType(JUSTIFY)
                        .lineHeight(1.4f)
                        .fullWidth(true)
                        .backgroundColor(null)
                        .border(noBorder())
                        .shadow(noShadow())
                        .vPadding(10)
                        .hPadding(10)
                        .topMargin(0)
                        .bottomMargin(0)
                        .borderRadius(0)
                        .build())
                .logoImage(null)
                .logoHeight(20)
                .logoAlign(MIDDLE);
    }
}
