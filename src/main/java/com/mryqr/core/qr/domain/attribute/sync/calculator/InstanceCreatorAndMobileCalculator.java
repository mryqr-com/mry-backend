package com.mryqr.core.qr.domain.attribute.sync.calculator;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.MemberMobileAttributeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CREATOR_AND_MOBILE;

@Component
@RequiredArgsConstructor
public class InstanceCreatorAndMobileCalculator implements AttributeValueCalculator {
    @Override
    public boolean canCalculate(Attribute attribute, App app) {
        return attribute.getType() == INSTANCE_CREATOR_AND_MOBILE;
    }

    @Override
    public AttributeValue calculate(Attribute attribute, QR qr, App app) {
        return new MemberMobileAttributeValue(attribute, qr.getCreatedBy());
    }
}
