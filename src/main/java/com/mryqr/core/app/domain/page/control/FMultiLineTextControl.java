package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.MinMaxSetting;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.multilinetext.MultiLineTextAnswer;
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

import static com.mryqr.core.common.exception.ErrorCode.MAX_OVERFLOW;
import static com.mryqr.core.common.exception.ErrorCode.MIN_OVERFLOW;
import static com.mryqr.core.common.exception.ErrorCode.MULTI_LINE_MAX_CONTENT_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.MULTI_LINE_MIN_CONTENT_NOT_REACHED;
import static com.mryqr.core.common.utils.MryConstants.MAX_PARAGRAPH_LENGTH;
import static com.mryqr.core.common.utils.MryConstants.MAX_PLACEHOLDER_LENGTH;
import static lombok.AccessLevel.PRIVATE;


@Getter
@SuperBuilder
@TypeAlias("MULTI_LINE_TEXT_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FMultiLineTextControl extends Control {
    @Size(max = MAX_PLACEHOLDER_LENGTH)
    private String placeholder;//占位符

    @Valid
    @NotNull
    private MinMaxSetting minMaxSetting;//最大字符数和最小字符数

    @Min(3)
    @Max(50)
    private int rows;//显示行数

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

    public MultiLineTextAnswer check(MultiLineTextAnswer answer) {
        if (answer.getContent().length() > minMaxSetting.getMax()) {
            failAnswerValidation(MULTI_LINE_MAX_CONTENT_REACHED, "填值超过了最大允许字符数。");
        }

        if (answer.isFilled() && answer.getContent().length() < minMaxSetting.getMin()) {
            failAnswerValidation(MULTI_LINE_MIN_CONTENT_NOT_REACHED, "填值未达到最小字符数。");
        }

        return answer;
    }

}
