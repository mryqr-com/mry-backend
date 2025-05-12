package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.exception.ErrorCode.NOT_ALL_ANSWERS_IN_RADIO_OPTIONS;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@SuperBuilder
@TypeAlias("RADIO_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FRadioControl extends AbstractTextOptionControl {
    public static final int MIN_OPTION_SIZE = 2;
    public static final int MAX_OPTION_SIZE = 20;

    @Override
    public void doCorrect(AppSettingContext context) {
        correctOptions();
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        validateOptions(MIN_OPTION_SIZE, MAX_OPTION_SIZE);
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        String optionId = optionIdFromName(value);
        if (isBlank(optionId)) {
            return null;
        }

        return RadioAnswer.answerBuilder(this).optionId(optionId).build();
    }

    public RadioAnswer check(RadioAnswer answer) {
        if (notContains(answer.getOptionId())) {
            failAnswerValidation(NOT_ALL_ANSWERS_IN_RADIO_OPTIONS, "答案不在单选框列表项中。");
        }

        return answer;
    }

}
