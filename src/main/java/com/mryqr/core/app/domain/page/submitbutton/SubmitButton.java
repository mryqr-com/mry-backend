package com.mryqr.core.app.domain.page.submitbutton;

import com.mryqr.core.app.domain.ui.ButtonStyle;
import com.mryqr.core.app.domain.ui.FontStyle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.mryqr.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static com.mryqr.core.app.domain.ui.border.Border.noBorder;
import static com.mryqr.core.app.domain.ui.shadow.Shadow.noShadow;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;


@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class SubmitButton {
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String text;//显示文本

    @Valid
    @NotNull
    private final ButtonStyle buttonStyle;//按钮样式

    public void correct() {
        if (isBlank(text)) {
            this.text = "提交";
        }
    }

    public static SubmitButton defaultSubmitButton() {
        return SubmitButton.builder()
                .text("提交")
                .buttonStyle(ButtonStyle.builder()
                        .fontStyle(FontStyle.builder()
                                .fontFamily("默认")
                                .fontSize(14)
                                .bold(true)
                                .italic(false)
                                .color("rgba(255, 255, 255, 1)")
                                .build())
                        .backgroundColor("#00bfff")
                        .border(noBorder())
                        .shadow(noShadow())
                        .vPadding(10)
                        .borderRadius(0)
                        .build())
                .build();
    }
}
