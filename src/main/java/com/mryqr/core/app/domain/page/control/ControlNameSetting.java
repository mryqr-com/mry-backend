package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.ui.BoxedTextStyle;
import com.mryqr.core.app.domain.ui.FontStyle;
import com.mryqr.core.app.domain.ui.VerticalPosition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.app.domain.ui.VerticalPosition.TOP;
import static com.mryqr.core.app.domain.ui.align.HorizontalAlignType.JUSTIFY;
import static com.mryqr.core.app.domain.ui.border.Border.noBorder;
import static com.mryqr.core.app.domain.ui.shadow.Shadow.noShadow;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ControlNameSetting {
    private boolean hidden;//是否隐藏

    @NotNull
    private VerticalPosition position;//标题位置

    @Valid
    @NotNull
    private BoxedTextStyle textStyle;//名称样式

    public static ControlNameSetting defaultControlNameSetting() {
        return ControlNameSetting.builder()
                .hidden(false)
                .position(TOP)
                .textStyle(BoxedTextStyle.builder()
                        .fontStyle(FontStyle.builder()
                                .fontFamily("默认")
                                .fontSize(15)
                                .bold(true)
                                .italic(false)
                                .color("#303133")
                                .build())
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
                        .borderRadius(0)
                        .build())
                .build();
    }

}
