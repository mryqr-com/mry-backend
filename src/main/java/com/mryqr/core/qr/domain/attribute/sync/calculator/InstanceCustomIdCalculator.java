package com.mryqr.core.qr.domain.attribute.sync.calculator;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.IdentifierAttributeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CUSTOM_ID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class InstanceCustomIdCalculator implements AttributeValueCalculator {
    @Override
    public boolean canCalculate(Attribute attribute, App app) {
        return attribute.getType() == INSTANCE_CUSTOM_ID;
    }

    @Override
    public AttributeValue calculate(Attribute attribute, QR qr, App app) {
        String customId = qr.getCustomId();
        return isNotBlank(customId) ? new IdentifierAttributeValue(attribute, customId) : null;
    }
}
