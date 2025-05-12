package com.mryqr.core.qr.domain.attribute.sync.calculator;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeType;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.DoubleAttributeValue;
import com.mryqr.core.qr.domain.attribute.IntegerAttributeValue;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_AVERAGE;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_MAX;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_MIN;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_SUM;
import static com.mryqr.core.common.domain.ValueType.INTEGER_VALUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionStatisticValueCalculator implements AttributeValueCalculator {
    private static final Set<AttributeType> SUPPORTED_TYPES = Set.of(CONTROL_SUM, CONTROL_AVERAGE, CONTROL_MAX, CONTROL_MIN);
    private final SubmissionRepository submissionRepository;

    @Override
    public boolean canCalculate(Attribute attribute, App app) {
        return SUPPORTED_TYPES.contains(attribute.getType());
    }

    public AttributeValue calculate(Attribute attribute, QR qr, App app) {
        Double doubleResult = submissionRepository.calculateStatisticValueForQr(attribute, qr, app);
        if (doubleResult == null) {
            return null;
        }

        if (attribute.getValueType() == INTEGER_VALUE) {
            return new IntegerAttributeValue(attribute, doubleResult.intValue());
        }

        return new DoubleAttributeValue(attribute, attribute.format(doubleResult));
    }
}

