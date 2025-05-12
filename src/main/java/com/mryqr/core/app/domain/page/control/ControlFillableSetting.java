package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.common.validation.nospace.NoSpace;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.mryqr.core.common.utils.MryConstants.MAX_SHORT_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class ControlFillableSetting {
    @NoSpace
    @Size(max = MAX_SHORT_NAME_LENGTH)
    private final String fieldName;//字段名称

    private final boolean submissionSummaryEligible;//是否为摘要字段，在提交历史控件（PSubmitHistoryControl）和手机端提交列表中是否直接显示

    private boolean mandatory;//是否必填

    private boolean autoFill;//自动填充上次提交值

    @Size(max = 100)
    private final String errorTips;//错误提示

    public static ControlFillableSetting defaultControlFillableSetting() {
        return defaultControlFillableSettingBuilder().build();
    }

    public static ControlFillableSettingBuilder defaultControlFillableSettingBuilder() {
        return ControlFillableSetting.builder().submissionSummaryEligible(true);
    }

    public void correct(ControlType type, Page page) {
        if (!(type.isAutoFillEligible() && page.requireLogin() && page.isNewSubmitType())) {
            this.autoFill = false;
        }
    }

    void setAutoFill(boolean value) {
        this.autoFill = value;
    }

    void setMandatory(boolean value) {
        this.mandatory = value;
    }
}
