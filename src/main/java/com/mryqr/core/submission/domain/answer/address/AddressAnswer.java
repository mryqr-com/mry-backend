package com.mryqr.core.submission.domain.answer.address;


import com.mryqr.common.domain.Address;
import com.mryqr.common.domain.display.AddressDisplayValue;
import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FAddressControl;
import com.mryqr.core.qr.domain.attribute.AddressAttributeValue;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("ADDRESS_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class AddressAnswer extends Answer {
    @Valid
    @NotNull
    private Address address;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return address != null && address.isFilled();
    }

    @Override
    public void clean(Control control) {
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return address.indexedValues();
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
        return new AddressAttributeValue(attribute, address);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new AddressDisplayValue(this.getControlId(), address);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        FAddressControl theControl = (FAddressControl) control;
        switch (theControl.getPrecision()) {
            case 1 -> {
                return address.getProvince();
            }
            case 2 -> {
                return Stream.of(address.getProvince(),
                                address.getCity())
                        .filter(StringUtils::isNotBlank).collect(joining());
            }
            case 3 -> {
                return Stream.of(address.getProvince(),
                                address.getCity(),
                                address.getDistrict())
                        .filter(StringUtils::isNotBlank).collect(joining());
            }
            default -> {
                return address.toText();
            }
        }
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    public static AddressAnswer.AddressAnswerBuilder<?, ?> answerBuilder(FAddressControl control) {
        return AddressAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
