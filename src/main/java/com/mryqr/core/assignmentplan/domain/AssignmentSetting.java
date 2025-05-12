package com.mryqr.core.assignmentplan.domain;

import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.id.app.AppId;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.joda.time.LocalDate;
import org.joda.time.Months;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static com.mryqr.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static com.mryqr.core.assignmentplan.domain.AssignmentFrequency.EVERY_DAY;
import static com.mryqr.core.assignmentplan.domain.AssignmentFrequency.EVERY_WEEK;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class AssignmentSetting {

    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String name;

    @AppId
    @NotBlank
    private final String appId;

    @PageId
    @NotBlank
    private final String pageId;

    @NotNull
    private final AssignmentFrequency frequency;

    @Valid
    @NotNull
    private final DateTime startTime;

    @Valid
    @NotNull
    private final DateTime expireTime;

    private final boolean nearExpireNotifyEnabled;

    @Valid
    @NotNull
    private DateTime nearExpireNotifyTime;

    public void correct() {
        if (!nearExpireNotifyEnabled) {
            nearExpireNotifyTime = DateTime.builder().build();
        }
    }

    public void validate(App app) {
        startTime.validate();
        expireTime.validate();

        if (!Objects.equals(app.getId(), appId)) {
            throw new RuntimeException("App ID and setting ID is not the same.");
        }

        Page page = app.pageById(pageId);
        requireNonNull(page);

        LocalDateTime startTime = this.startTime.toLocalDateTime();
        LocalDateTime expireTime = this.expireTime.toLocalDateTime();

        if (!startTime.isBefore(expireTime)) {
            throw new MryException(ASSIGNMENT_START_TIME_AFTER_END_TIME, "创建任务计划失败，任务结束时间必须晚于任务开始时间。");
        }

        LocalDateTime nextStartTime = timeForFutureCycle(this.startTime.toLocalDateTime(), 1);

        if (expireTime.isAfter(nextStartTime)) {
            throw new MryException(ASSIGNMENT_DURATION_EXCEED_FREQUENCY,
                    "任务结束时间不能超过下次任务开始时间（" + MRY_DATE_TIME_FORMATTER.format(nextStartTime) + "）。");
        }

        if (nearExpireNotifyEnabled) {
            requireNonNull(nearExpireNotifyTime);
            nearExpireNotifyTime.validate();

            LocalDateTime nearExpireNotifyTime = this.nearExpireNotifyTime.toLocalDateTime();
            if (!(nearExpireNotifyTime.isAfter(startTime) && nearExpireNotifyTime.isBefore(expireTime))) {
                throw new MryException(ASSIGNMENT_NOTIFY_TIME_OVERFLOW, "即将超期提醒时间必须位于任务开始时间和结束时间之间。");
            }
        }
    }

    public boolean startTimeMatches(LocalDateTime givenStartTime) {
        LocalDateTime startTime = this.startTime.toLocalDateTime();

        if (givenStartTime.isBefore(startTime)) {
            return false;
        }

        if (frequency == EVERY_DAY) {
            return startTime.getHour() == givenStartTime.getHour();
        }

        if (frequency == EVERY_WEEK) {
            return startTime.getHour() == givenStartTime.getHour() && startTime.getDayOfWeek() == givenStartTime.getDayOfWeek();
        }

        //极端情况：对于EVERY_MONTH来说，最多可保证运行20年(240/12，从2023年开始算起)，20年后如果项目还在，记得修复
        return range(0, 240).anyMatch(index -> givenStartTime.equals(timeForFutureCycle(this.startTime.toLocalDateTime(), index)));
    }

    public long cycleIndexOf(LocalDateTime givenStartTime) {
        return switch (frequency) {
            case EVERY_DAY -> secondsBetween(this.startTime.toLocalDateTime(), givenStartTime) / (24 * 3600);
            case EVERY_WEEK -> secondsBetween(this.startTime.toLocalDateTime(), givenStartTime) / (7 * 24 * 3600);
            case EVERY_MONTH -> monthsBetween(this.startTime.toLocalDateTime(), givenStartTime);
            case EVERY_THREE_MONTH -> monthsBetween(this.startTime.toLocalDateTime(), givenStartTime) / 3;
            case EVERY_SIX_MONTH -> monthsBetween(this.startTime.toLocalDateTime(), givenStartTime) / 6;
            case EVERY_YEAR -> monthsBetween(this.startTime.toLocalDateTime(), givenStartTime) / 12;
        };
    }

    public LocalDateTime expireAtFor(long cycleIndex) {
        return timeForFutureCycle(this.expireTime.toLocalDateTime(), cycleIndex);
    }

    public LocalDateTime nearExpireNotifyAtFor(long cycleIndex) {
        return timeForFutureCycle(this.nearExpireNotifyTime.toLocalDateTime(), cycleIndex);
    }

    public LocalDateTime nextAssignmentStartAt() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = this.startTime.toLocalDateTime();

        if (now.isBefore(startTime)) {
            return startTime;
        }

        long cycleIndex = cycleIndexOf(now);
        return timeForFutureCycle(this.startTime.toLocalDateTime(), cycleIndex + 1);
    }

    public long secondsBetweenStartAndExpire() {
        return secondsBetween(this.startTime.toLocalDateTime(), expireTime.toLocalDateTime());
    }

    public long secondsBetweenStartAndNearEndNotify() {
        return secondsBetween(this.startTime.toLocalDateTime(), nearExpireNotifyTime.toLocalDateTime());
    }

    private LocalDateTime timeForFutureCycle(LocalDateTime initialTime, long cycleIndex) {
        LocalDateTime nextTime = switch (frequency) {
            case EVERY_DAY -> initialTime.plus(cycleIndex, DAYS);
            case EVERY_WEEK -> initialTime.plus(7 * cycleIndex, DAYS);
            case EVERY_MONTH -> initialTime.plus(cycleIndex, MONTHS);
            case EVERY_THREE_MONTH -> initialTime.plus(3 * cycleIndex, MONTHS);
            case EVERY_SIX_MONTH -> initialTime.plus(6 * cycleIndex, MONTHS);
            case EVERY_YEAR -> initialTime.plus(12 * cycleIndex, MONTHS);
        };

        if (this.frequency.isFixedTimeCycle()) {
            return nextTime;
        }

        if (isLastDayOfMonth(initialTime)) {//处理月份的最后一天，即首次是月的最后一天，本次依然应该为月的最后一天
            return nextTime.with(lastDayOfMonth());
        }

        return nextTime;
    }

    private boolean isLastDayOfMonth(LocalDateTime time) {
        return time.with(lastDayOfMonth()).getDayOfMonth() == time.getDayOfMonth();
    }

    private long secondsBetween(LocalDateTime time1, LocalDateTime time2) {
        return Duration.between(time1, time2).getSeconds();
    }

    private int monthsBetween(LocalDateTime time1, LocalDateTime time2) {
        LocalDate localDate1 = new LocalDate(time1.getYear(), time1.getMonthValue(), time1.getDayOfMonth());
        LocalDate localDate2 = new LocalDate(time2.getYear(), time2.getMonthValue(), time2.getDayOfMonth());
        return Months.monthsBetween(localDate1, localDate2).getMonths();
    }
}
