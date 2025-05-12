package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.exception.MryException;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.MinMaxSetting;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.richtext.RichTextInputAnswer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MryConstants.MAX_PARAGRAPH_LENGTH;
import static com.mryqr.common.utils.MryConstants.MAX_PLACEHOLDER_LENGTH;
import static lombok.AccessLevel.PRIVATE;


@Getter
@SuperBuilder
@TypeAlias("RICH_TEXT_INPUT_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FRichTextInputControl extends Control {
    @Size(max = MAX_PLACEHOLDER_LENGTH)
    private String placeholder;//占位符

    @Valid
    @NotNull
    private MinMaxSetting minMaxSetting;//最大字符数和最小字符数

    @Override
    protected void doCorrect(AppSettingContext context) {
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        minMaxSetting.validate();
        if (minMaxSetting.getMax() > MAX_PARAGRAPH_LENGTH) {
            throw new MryException(MAX_OVERFLOW, "最大字符数超出限制。");
        }

        if (minMaxSetting.getMin() < 0) {
            throw new MryException(MIN_OVERFLOW, "最小字符数超出限制。");
        }
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

    public RichTextInputAnswer check(RichTextInputAnswer answer) {
        if (answer.getContent().length() > minMaxSetting.getMax()) {
            failAnswerValidation(RICH_TEXT_MAX_CONTENT_REACHED, "填值超过了最大允许字符数。");
        }

        if (answer.isFilled() && answer.getContent().length() < minMaxSetting.getMin()) {
            failAnswerValidation(RICH_TEXT_MIN_CONTENT_NOT_REACHED, "填值未达到最小字符数。");
        }

        return answer;
    }

}
