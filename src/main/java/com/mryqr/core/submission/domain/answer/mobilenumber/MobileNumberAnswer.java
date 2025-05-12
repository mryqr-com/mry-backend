package com.mryqr.core.submission.domain.answer.mobilenumber;


import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FMobileNumberControl;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.TextDisplayValue;
import com.mryqr.core.common.validation.mobile.Mobile;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.MobileAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("MOBILE_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class MobileNumberAnswer extends Answer {

    @Mobile
    private String mobileNumber;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return isNotBlank(mobileNumber);
    }

    @Override
    public void clean(Control control) {
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return Set.of(mobileNumber);
    }

    @Override
    protected Double doGetIndexedSortableValue() {
        return null;
    }

    @Override
    protected Set<String> doGetSearchableValues() {
        return Set.of(mobileNumber);
    }

    @Override
    protected AttributeValue doGetAttributeValue(Attribute attribute, Control control) {
        return new MobileAttributeValue(attribute, mobileNumber);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new TextDisplayValue(this.getControlId(), mobileNumber);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return mobileNumber;
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    public static MobileNumberAnswer.MobileNumberAnswerBuilder<?, ?> answerBuilder(FMobileNumberControl control) {
        return MobileNumberAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
