package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.time.TimeAnswer;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static com.mryqr.common.utils.MryConstants.MAX_PLACEHOLDER_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Getter
@SuperBuilder
@TypeAlias("TIME_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FTimeControl extends Control {
    private static final DateTimeFormatter FROM_STRING_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("H:m"))
            .toFormatter();

    @Size(max = MAX_PLACEHOLDER_LENGTH)
    private String placeholder;//占位符

    private boolean defaultToNow;

    @Override
    protected void doCorrect(AppSettingContext context) {
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        try {
            String parsed = LocalTime.parse(value, FROM_STRING_FORMATTER).toString();
            return TimeAnswer.answerBuilder(this).time(parsed).build();
        } catch (Exception e) {
            log.warn("Can't parse time: {}, will skip it.", value, e);
            return null;
        }
    }

    public TimeAnswer check(TimeAnswer answer) {
        return answer;
    }

}
