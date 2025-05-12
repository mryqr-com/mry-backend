package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.mobilenumber.MobileNumberAnswer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.utils.MryConstants.MAX_PLACEHOLDER_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("MOBILE_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FMobileNumberControl extends Control {
    @Size(max = MAX_PLACEHOLDER_LENGTH)
    private String placeholder;//占位符

    @NotNull
    private AnswerUniqueType uniqueType;//唯一性检查类型

    @Override
    protected void doCorrect(AppSettingContext context) {
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return MobileNumberAnswer.answerBuilder(this).mobileNumber(value).build();
    }

    public MobileNumberAnswer check(MobileNumberAnswer answer) {
        return answer;
    }

}
