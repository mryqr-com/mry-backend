package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("SECTION_TITLE_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PSectionTitleViewControl extends Control {

    @Override
    public void doCorrect(AppSettingContext context) {
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

}
