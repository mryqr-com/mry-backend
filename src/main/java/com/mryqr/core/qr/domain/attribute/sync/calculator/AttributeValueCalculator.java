package com.mryqr.core.qr.domain.attribute.sync.calculator;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;

public interface AttributeValueCalculator {
    boolean canCalculate(Attribute attribute, App app);

    AttributeValue calculate(Attribute attribute, QR qr, App app);
}
