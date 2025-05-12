package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.exception.MryException;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.MinMaxSetting;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MryConstants.MAX_PLACEHOLDER_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Getter
@SuperBuilder
@TypeAlias("DROPDOWN_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FDropdownControl extends AbstractTextOptionControl {
    public static final int MIN_OPTION_SIZE = 2;
    public static final int MAX_OPTION_SIZE = 100;

    public static final int MIN_SELECTION = 0;
    public static final int MAX_SELECTION = MAX_OPTION_SIZE;

    @Size(max = MAX_PLACEHOLDER_LENGTH)
    private String placeholder;//占位符

    private boolean multiple;//是否多选

    private boolean filterable;//是否可搜索

    @Valid
    @NotNull
    private MinMaxSetting minMaxSetting;//最大和最小可选数

    @Override
    public void doCorrect(AppSettingContext context) {
        correctOptions();

        if (!multiple) {
            this.minMaxSetting = MinMaxSetting.builder().min(0).max(10).build();
        }
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

        return DropdownAnswer.answerBuilder(this).optionIds(optionIds).build();
    }

    public DropdownAnswer check(DropdownAnswer answer) {
        if (notContainsAll(answer.getOptionIds())) {
            failAnswerValidation(NOT_ALL_ANSWERS_IN_DROPDOWN_OPTIONS, "答案不全在控件选项列表项中。");
        }

        if (multiple) {
            if (answer.getOptionIds().size() > minMaxSetting.getMax()) {
                failAnswerValidation(DROPDOWN_MAX_SELECTION_REACHED, "选项数量已超过最大允许值。");
            }

            if (answer.isFilled() && answer.getOptionIds().size() < minMaxSetting.getMin()) {
                failAnswerValidation(DROPDOWN_MIN_SELECTION_NOT_REACHED, "选项数量未达到最小值。");
            }
        }

        if (!multiple && answer.getOptionIds().size() > 1) {
            failAnswerValidation(SINGLE_DROPDOWN_ONLY_ALLOW_SINGLE_ANSWER, "单选下拉框最多只能包含1个答案选项。");
        }

        return answer;
    }

}
