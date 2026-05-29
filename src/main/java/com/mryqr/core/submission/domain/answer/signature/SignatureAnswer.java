package com.mryqr.core.submission.domain.answer.signature;


import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.FilesDisplayValue;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FSignatureControl;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.SignatureAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("SIGNATURE_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class SignatureAnswer extends Answer {
    @Valid
    private UploadedFile signature;

    @Override
    public void correctAndValidate() {

    }

    @Override
    public boolean isFilled() {
        return signature != null;
    }

    @Override
    public void clean(Control control) {
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return null;
    }

    @Override
    protected Double doGetIndexedSortableValue() {
        return null;
    }

    @Override
    protected Set<String> doGetSearchableValues() {
        return null;
    }

    @Override
    protected AttributeValue doGetAttributeValue(Attribute attribute, Control control) {
        return new SignatureAttributeValue(attribute, signature);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new FilesDisplayValue(this.getControlId(), List.of(signature));
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return null;
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    public static SignatureAnswer.SignatureAnswerBuilder<?, ?> answerBuilder(FSignatureControl control) {
        return SignatureAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
