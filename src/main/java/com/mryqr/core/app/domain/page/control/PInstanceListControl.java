package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("INSTANCE_LIST_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PInstanceListControl extends Control {
    @Min(1)
    @Max(100)
    private int max;//最大显示条数

    private boolean showCreatedAt;//显示创建日期

    private boolean showCreator;//显示创建人

    private boolean showSeparator;//显示分割线

    @Min(0)
    @Max(50)
    private int rowGutter;//实例行之间的间距

    @Override
    protected void doCorrect(AppSettingContext context) {

    }

    @Override
    protected void doValidate(AppSettingContext context) {

    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }
}
