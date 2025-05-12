package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.MinMaxSetting;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.app.domain.page.control.IdentifierFormatType.REGEX;
import static com.mryqr.core.common.exception.ErrorCode.IDENTIFIER_MAX_CONTENT_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.IDENTIFIER_MIN_CONTENT_NOT_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.MAX_OVERFLOW;
import static com.mryqr.core.common.exception.ErrorCode.MIN_OVERFLOW;
import static com.mryqr.core.common.utils.MryConstants.MAX_PLACEHOLDER_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("IDENTIFIER_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FIdentifierControl extends Control {
    public static final int MIN_IDENTIFIER_LENGTH = 0;
    public static final int MAX_IDENTIFIER_LENGTH = 50;
    public static final int MAX_FORMAT_REGEX_LENGTH = 500;

    @Size(max = MAX_PLACEHOLDER_LENGTH)
    private String placeholder;//占位符

    @NotNull
    private AnswerUniqueType uniqueType;//唯一性检查类型

    @Valid
    @NotNull
    private MinMaxSetting minMaxSetting;//最大字符数和最小字符数

    @NotNull
    private IdentifierFormatType identifierFormatType;

    @Size(max = MAX_FORMAT_REGEX_LENGTH)
    private String formatRegex;

    @Override
    protected void doCorrect(AppSettingContext context) {
        if (identifierFormatType != REGEX) {
            formatRegex = null;
        }
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        minMaxSetting.validate();
        if (minMaxSetting.getMax() > MAX_IDENTIFIER_LENGTH) {
            throw new MryException(MAX_OVERFLOW, "最大字符数超出限制。");
        }

        if (minMaxSetting.getMin() < MIN_IDENTIFIER_LENGTH) {
            throw new MryException(MIN_OVERFLOW, "未达到最小字符数。");
        }
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return IdentifierAnswer.answerBuilder(this).content(value).build();
    }

    public IdentifierAnswer check(IdentifierAnswer answer) {
        if (answer.getContent().length() > minMaxSetting.getMax()) {
            failAnswerValidation(IDENTIFIER_MAX_CONTENT_REACHED, "填值超过了最大允许字符数。");
        }

        if (answer.isFilled() && answer.getContent().length() < minMaxSetting.getMin()) {
            failAnswerValidation(IDENTIFIER_MIN_CONTENT_NOT_REACHED, "填值未达到最小字符数。");
        }

        return answer;
    }

}
