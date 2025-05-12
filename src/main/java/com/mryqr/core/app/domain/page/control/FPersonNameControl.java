package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.personname.PersonNameAnswer;
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
@TypeAlias("PERSON_NAME_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FPersonNameControl extends Control {

    @Size(max = MAX_PLACEHOLDER_LENGTH)
    private String placeholder;//占位符

    @Override
    protected void doCorrect(AppSettingContext context) {
    }

    @Override
    protected void doValidate(AppSettingContext context) {

    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return PersonNameAnswer.answerBuilder(this).name(value).build();
    }

    public PersonNameAnswer check(PersonNameAnswer answer) {
        return answer;
    }

}
