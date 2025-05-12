package com.mryqr.core.qr.domain.attribute.sync.calculator;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.LocalDateAttributeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CREATE_DATE;
import static java.time.LocalDate.ofInstant;
import static java.time.ZoneId.systemDefault;

@Component
@RequiredArgsConstructor
public class InstanceCreateDateCalculator implements AttributeValueCalculator {

    @Override
    public boolean canCalculate(Attribute attribute, App app) {
        return attribute.getType() == INSTANCE_CREATE_DATE;
    }

    @Override
    public AttributeValue calculate(Attribute attribute, QR qr, App app) {
        return new LocalDateAttributeValue(attribute, ofInstant(qr.getCreatedAt(), systemDefault()).toString());
    }
}
