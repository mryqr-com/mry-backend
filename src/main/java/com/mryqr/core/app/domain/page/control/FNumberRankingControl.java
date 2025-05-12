package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.numberranking.NumberRankingAnswer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.exception.ErrorCode.MAX_RANK_REACHED;
import static java.lang.Double.valueOf;
import static java.lang.Integer.parseInt;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("NUMBER_RANKING_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FNumberRankingControl extends Control {
    public static final int MIN_RANKING_LIMIT = 5;
    public static final int MAX_RANKING_LIMIT = 10;

    @Min(MIN_RANKING_LIMIT)
    @Max(MAX_RANKING_LIMIT)
    private int max;//最大值

    @Override
    protected void doCorrect(AppSettingContext context) {
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    public NumberRankingAnswer check(NumberRankingAnswer answer) {
        if (answer.getRank() > max) {
            failAnswerValidation(MAX_RANK_REACHED, "打分大于最大限制。");
        }
        return answer;
    }

    public Double format(Double number) {
        return number == null ? null : valueOf(String.format("%.0f", number));
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        try {
            int theValue = parseInt(value);
            return NumberRankingAnswer.answerBuilder(this).rank(theValue).build();
        } catch (NumberFormatException ex) {
            return null;
        }
    }

}
