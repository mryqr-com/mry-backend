package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.MinMaxSetting;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.exception.ErrorCode.MAX_OVERFLOW;
import static com.mryqr.core.common.exception.ErrorCode.MIN_OVERFLOW;
import static com.mryqr.core.common.exception.ErrorCode.SINGLE_LINE_MAX_CONTENT_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.SINGLE_LINE_MIN_CONTENT_NOT_REACHED;
import static com.mryqr.core.common.utils.MryConstants.MAX_PLACEHOLDER_LENGTH;
import static lombok.AccessLevel.PRIVATE;


@Getter
@SuperBuilder
@TypeAlias("SINGLE_LINE_TEXT_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FSingleLineTextControl extends Control {
    public static final int MIN_ANSWER_LENGTH = 0;
    public static final int MAX_ANSWER_LENGTH = 100;

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
        if (minMaxSetting.getMax() > MAX_ANSWER_LENGTH) {
            throw new MryException(MAX_OVERFLOW, "最大字符数超出限制。");
        }

        if (minMaxSetting.getMin() < MIN_ANSWER_LENGTH) {
            throw new MryException(MIN_OVERFLOW, "最小字符数超出限制。");
        }
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return SingleLineTextAnswer.answerBuilder(this).content(value).build();
    }

    public SingleLineTextAnswer check(SingleLineTextAnswer answer) {
        if (answer.getContent().length() > minMaxSetting.getMax()) {
            failAnswerValidation(SINGLE_LINE_MAX_CONTENT_REACHED, "填值超过了最大允许字符数。");
        }

        if (answer.isFilled() && answer.getContent().length() < minMaxSetting.getMin()) {
            failAnswerValidation(SINGLE_LINE_MIN_CONTENT_NOT_REACHED, "填值未达到最小字符数。");
        }

        return answer;
    }

}
