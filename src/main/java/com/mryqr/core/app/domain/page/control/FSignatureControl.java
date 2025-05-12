package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.ButtonStyle;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.signature.SignatureAnswer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.utils.MryConstants.MAX_SHORT_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@SuperBuilder
@TypeAlias("SIGNATURE_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FSignatureControl extends Control {
    @Valid
    @NotNull
    private ButtonStyle buttonStyle;//按钮样式

    @Size(max = MAX_SHORT_NAME_LENGTH)
    private String buttonText;//按钮文本

    @Override
    protected void doCorrect(AppSettingContext context) {
        if (isBlank(buttonText)) {
            this.buttonText = "签名";
        }
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

    public Answer check(SignatureAnswer answer) {
        return answer;
    }

}
