package com.mryqr.core.qr.domain.attribute.sync.calculator;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.BooleanAttributeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_TEMPLATE_STATUS;

@Component
@RequiredArgsConstructor
public class InstanceTemplateStatusCalculator implements AttributeValueCalculator {
    @Override
    public boolean canCalculate(Attribute attribute, App app) {
        return attribute.getType() == INSTANCE_TEMPLATE_STATUS;
    }

    @Override
    public AttributeValue calculate(Attribute attribute, QR qr, App app) {
        return new BooleanAttributeValue(attribute, qr.isTemplate());
    }
}
