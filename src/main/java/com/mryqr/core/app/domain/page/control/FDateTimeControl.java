package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.datetime.DateTimeAnswer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.time.LocalDateTime;

import static com.mryqr.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static lombok.AccessLevel.PRIVATE;


@Getter
@SuperBuilder
@TypeAlias("DATE_TIME_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FDateTimeControl extends Control {
    private boolean defaultToNow;

    @Override
    protected void doCorrect(AppSettingContext context) {
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        LocalDateTime parsed = LocalDateTime.parse(value, MRY_DATE_TIME_FORMATTER);
        return DateTimeAnswer.answerBuilder(this)
                .date(parsed.toLocalDate().toString())
                .time(parsed.toLocalTime().toString())
                .build();
    }

    public DateTimeAnswer check(DateTimeAnswer answer) {
        return answer;
    }

}
