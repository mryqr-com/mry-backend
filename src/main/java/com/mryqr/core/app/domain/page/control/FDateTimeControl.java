package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.datetime.DateTimeAnswer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.ofPattern;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Getter
@SuperBuilder
@TypeAlias("DATE_TIME_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FDateTimeControl extends Control {
    private static final DateTimeFormatter FROM_STRING_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(ofPattern("u-M-d H:m"))
            .appendOptional(ofPattern("u/M/d H:m"))
            .toFormatter();

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
            LocalDateTime parsed = LocalDateTime.parse(value, FROM_STRING_FORMATTER);
            return DateTimeAnswer.answerBuilder(this)
                    .date(parsed.toLocalDate().toString())
                    .time(parsed.toLocalTime().toString())
                    .build();
        } catch (Exception e) {
            log.warn("Can't parse date time: {}, will skip it.", value, e);
            return null;
        }
    }

    public DateTimeAnswer check(DateTimeAnswer answer) {
        return answer;
    }

}
