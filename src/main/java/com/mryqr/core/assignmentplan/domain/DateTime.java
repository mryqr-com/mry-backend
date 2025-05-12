package com.mryqr.core.assignmentplan.domain;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDateTime;

import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.core.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static com.mryqr.core.common.utils.MryRegexConstants.DATE_PATTERN;
import static com.mryqr.core.common.utils.MryRegexConstants.TIME_PATTERN;
import static java.time.ZoneId.systemDefault;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class DateTime {

    @Pattern(regexp = DATE_PATTERN, message = "日期格式不正确。")
    private final String date;

    @Pattern(regexp = TIME_PATTERN, message = "时间格式不正确。")
    private final String time;

    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.parse(date + " " + time, MRY_DATE_TIME_FORMATTER);
    }

    public Instant toInstant() {
        return this.toLocalDateTime().atZone(systemDefault()).toInstant();
    }

    public void validate() {
        requireNonBlank(date, "Date must not be blank.");
        requireNonBlank(time, "Time must not be blank.");
    }
}
