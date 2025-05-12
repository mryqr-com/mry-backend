package com.mryqr.core.submission.domain.answer.geolocation;


import com.mryqr.common.domain.Geolocation;
import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.GeolocationDisplayValue;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FGeolocationControl;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.GeolocationAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("GEOLOCATION_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class GeolocationAnswer extends Answer {
    @Valid
    @NotNull
    private Geolocation geolocation;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return geolocation != null && geolocation.isPositioned();
    }

    @Override
    public void clean(Control control) {
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return geolocation.indexedValues();
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
        return new GeolocationAttributeValue(attribute, geolocation);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new GeolocationDisplayValue(this.getControlId(), geolocation);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return geolocation.toText();
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    public static GeolocationAnswer.GeolocationAnswerBuilder<?, ?> answerBuilder(FGeolocationControl control) {
        return GeolocationAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
