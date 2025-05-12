package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.exception.MryException;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.MinMaxSetting;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.checkbox.CheckboxAnswer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;

import static com.mryqr.common.exception.ErrorCode.*;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Getter
@SuperBuilder
@TypeAlias("CHECKBOX_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FCheckboxControl extends AbstractTextOptionControl {
    public static final int MIN_OPTION_SIZE = 2;
    public static final int MAX_OPTION_SIZE = 20;
    public static final int MIN_SELECTION = 0;
    public static final int MAX_SELECTION = MAX_OPTION_SIZE;

    @Valid
    @NotNull
    private MinMaxSetting minMaxSetting;//最大和最小可选项

    @Override
    public void doCorrect(AppSettingContext context) {
        correctOptions();
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        validateOptions(MIN_OPTION_SIZE, MAX_OPTION_SIZE);
        minMaxSetting.validate();

        if (minMaxSetting.getMax() > MAX_SELECTION) {
            throw new MryException(MAX_OVERFLOW, "最大可选数超出限制。");
        }

        if (minMaxSetting.getMin() < MIN_SELECTION) {
            throw new MryException(MIN_OVERFLOW, "最小可选数超出限制。");
        }
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        List<String> optionIds = optionIdsFromNames(value);
        if (isEmpty(optionIds)) {
            return null;
        }

        return CheckboxAnswer.answerBuilder(this).optionIds(optionIds).build();
    }

    public CheckboxAnswer check(CheckboxAnswer answer) {
        if (notContainsAll(answer.getOptionIds())) {
            failAnswerValidation(NOT_ALL_ANSWERS_IN_CHECKBOX_OPTIONS, "答案不全在控件选项列表项中。");
        }

        if (answer.getOptionIds().size() > minMaxSetting.getMax()) {
            failAnswerValidation(CHECKBOX_MAX_SELECTION_REACHED, "选项数量超过了最大允许值。");
        }

        if (answer.isFilled() && answer.getOptionIds().size() < minMaxSetting.getMin()) {
            failAnswerValidation(CHECKBOX_MIN_SELECTION_NOT_REACHED, "选项数量未达到最小要求值。");
        }

        return answer;
    }

}
