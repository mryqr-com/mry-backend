package com.mryqr.core.qr.domain.attribute.sync.calculator;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.CirculationStatusAttributeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CIRCULATION_STATUS;

@Component
@RequiredArgsConstructor
public class InstanceCirculationStatusCalculator implements AttributeValueCalculator {

    @Override
    public boolean canCalculate(Attribute attribute, App app) {
        return attribute.getType() == INSTANCE_CIRCULATION_STATUS;
    }

    @Override
    public AttributeValue calculate(Attribute attribute, QR qr, App app) {
        return new CirculationStatusAttributeValue(attribute, qr.getCirculationOptionId());
    }
}
