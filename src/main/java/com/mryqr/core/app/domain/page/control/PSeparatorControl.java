package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.validation.color.Color;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.FontStyle;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("SEPARATOR_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PSeparatorControl extends Control {
    @NotNull
    private SeparatorType separatorType;//边框类型

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String text;//中央文本

    @Valid
    @NotNull
    private FontStyle fontStyle;//中央文本样式

    @Min(50)
    @Max(100)
    private int widthRatio;//宽度比

    @Color
    private String backgroundColor;//背景色

    @Min(1)
    @Max(5)
    private int borderWidth;//分隔符宽度

    @Override
    public void doCorrect(AppSettingContext context) {
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

    public enum SeparatorType {
        SOLID,
        DASHED,
        DOTTED,
    }
}
