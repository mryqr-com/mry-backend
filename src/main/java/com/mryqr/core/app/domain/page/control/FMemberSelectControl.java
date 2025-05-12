package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.exception.MryException;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.MinMaxSetting;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.memberselect.MemberSelectAnswer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MryConstants.MAX_PLACEHOLDER_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("MEMBER_SELECT_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FMemberSelectControl extends Control {
    public static final int MIN_MEMBER_SELECTION = 0;
    public static final int MAX_MEMBER_SELECTION = 100;

    @Size(max = MAX_PLACEHOLDER_LENGTH)
    private String placeholder;//占位符

    private boolean multiple;//是否可多选

    private boolean filterable;//可搜索

    @Valid
    @NotNull
    private MinMaxSetting minMaxSetting;//最大和最小可选成员数

    @Override
    protected void doCorrect(AppSettingContext context) {
        if (!multiple) {
            this.minMaxSetting = MinMaxSetting.builder().min(0).max(10).build();
        }
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        minMaxSetting.validate();
        if (minMaxSetting.getMax() > MAX_MEMBER_SELECTION) {
            throw new MryException(MAX_OVERFLOW, "最大可选成员数超出限制。");
        }

        if (minMaxSetting.getMin() < MIN_MEMBER_SELECTION) {
            throw new MryException(MIN_OVERFLOW, "最小可选成员数超出限制。");
        }
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

    public MemberSelectAnswer check(MemberSelectAnswer answer) {
        if (multiple) {
            if (answer.getMemberIds().size() > minMaxSetting.getMax()) {
                failAnswerValidation(MEMBER_MAX_SELECTION_REACHED, "所选成员数量已超过最大允许值。");
            }

            if (answer.isFilled() && answer.getMemberIds().size() < minMaxSetting.getMin()) {
                failAnswerValidation(MEMBER_MIN_SELECTION_NOT_REACHED, "所选成员数量未达到最小值。");
            }
        }

        if (!multiple && answer.getMemberIds().size() > 1) {
            failAnswerValidation(SINGLE_MEMBER_ONLY_ALLOW_SINGLE_ANSWER, "单选成员项最多只能包含1个答案选项。");
        }

        return answer;
    }

}
