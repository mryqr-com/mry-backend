package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.pointcheck.PointCheckAnswer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.core.common.exception.ErrorCode.NOT_ALL_POINT_CHECK_ANSWERED;
import static com.mryqr.core.common.exception.ErrorCode.ONLY_PARTIAL_POINT_CHECK_ANSWERED;
import static com.mryqr.core.common.exception.ErrorCode.POINT_CHECK_ANSWER_NOT_MATCH_TO_CONTROL;
import static com.mryqr.core.submission.domain.answer.pointcheck.PointCheckValue.NONE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("POINT_CHECK_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FPointCheckControl extends AbstractTextOptionControl {
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
        return null;
    }

    public PointCheckAnswer check(PointCheckAnswer answer) {
        Set<String> answeredIds = answer.getChecks().keySet();

        if (!allOptionIds().equals(answeredIds)) {
            failAnswerValidation(POINT_CHECK_ANSWER_NOT_MATCH_TO_CONTROL, "所提供点检项与控件不完全匹配。");
        }

        if (isMandatory()) {
            if (answer.getChecks().values().stream().anyMatch(v -> v == NONE)) {
                failAnswerValidation(NOT_ALL_POINT_CHECK_ANSWERED, "未完成所有点检项。");
            }
        } else {
            //要么全部填，要么全部不填，否则部分填的情况不允许
            if (!(answer.getChecks().values().stream().allMatch(v -> v == NONE) ||
                    answer.getChecks().values().stream().allMatch(v -> v != NONE))) {
                failAnswerValidation(ONLY_PARTIAL_POINT_CHECK_ANSWERED, "点检项不完整。");
            }
        }

        return answer;
    }

}
