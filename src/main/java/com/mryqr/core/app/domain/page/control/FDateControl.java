package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.mryqr.common.utils.MryConstants.MAX_PLACEHOLDER_LENGTH;
import static java.time.format.DateTimeFormatter.ofPattern;
import static lombok.AccessLevel.PRIVATE;


@Getter
@SuperBuilder
@TypeAlias("DATE_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FDateControl extends Control {
    private static final DateTimeFormatter FORMATTER = ofPattern("u-M-d");

    @Size(max = MAX_PLACEHOLDER_LENGTH)
    private String placeholder;//占位符

    private boolean defaultToToday;

    @Override
    protected void doCorrect(AppSettingContext context) {
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        String parsed = LocalDate.parse(value, FORMATTER).toString();
        return DateAnswer.answerBuilder(this).date(parsed).build();
    }

    public DateAnswer check(DateAnswer answer) {
        return answer;
    }

}
