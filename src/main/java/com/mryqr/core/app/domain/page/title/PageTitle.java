package com.mryqr.core.app.domain.page.title;

import com.mryqr.core.app.domain.ui.BoxedTextStyle;
import com.mryqr.core.app.domain.ui.FontStyle;
import com.mryqr.core.app.domain.ui.MarkdownStyle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.mryqr.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static com.mryqr.common.utils.MryConstants.MAX_PARAGRAPH_LENGTH;
import static com.mryqr.core.app.domain.ui.align.HorizontalAlignType.CENTER;
import static com.mryqr.core.app.domain.ui.border.Border.noBorder;
import static com.mryqr.core.app.domain.ui.shadow.Shadow.noShadow;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class PageTitle {
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String text;//标题文本

    @Valid
    @NotNull
    private final BoxedTextStyle textStyle;//标题样式

    @Size(max = MAX_PARAGRAPH_LENGTH)
    private final String description;//描述文本

    @Valid
    @NotNull
    private final MarkdownStyle descriptionStyle;//描述文本样式

    public void correct() {
        if (isBlank(text)) {
            this.text = "点击编辑标题";
        }
    }

    public static PageTitle defaultPageTitle() {
        return defaultPageTitleBuilder().build();
    }

    public static PageTitleBuilder defaultPageTitleBuilder() {
        return PageTitle.builder()
                .text("点击编辑标题")
                .textStyle(BoxedTextStyle.builder()
                        .fontStyle(FontStyle.builder()
                                .fontFamily("默认")
                                .fontSize(20)
                                .bold(true)
                                .italic(false)
                                .color("#303133")
                                .build())
                        .alignType(CENTER)
                        .lineHeight(1.4f)
                        .fullWidth(true)
                        .backgroundColor(null)
                        .border(noBorder())
                        .shadow(noShadow())
                        .vPadding(0)
                        .hPadding(0)
                        .topMargin(30)
                        .bottomMargin(20)
                        .borderRadius(4)
                        .build())
                .description(null)
                .descriptionStyle(MarkdownStyle.builder()
                        .fontStyle(FontStyle.builder()
                                .fontFamily("默认")
                                .fontSize(14)
                                .bold(false)
                                .italic(false)
                                .color("#444")
                                .build())
                        .lineHeight(1.6f)
                        .build());
    }
}
