package com.mryqr.core.qr.domain.attribute.sync.calculator;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.IntegerAttributeValue;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_SUBMIT_COUNT;

@Component
@RequiredArgsConstructor
public class InstanceSubmitCountCalculator implements AttributeValueCalculator {
    private final SubmissionRepository submissionRepository;

    @Override
    public boolean canCalculate(Attribute attribute, App app) {
        return attribute.getType() == INSTANCE_SUBMIT_COUNT;
    }

    @Override
    public AttributeValue calculate(Attribute attribute, QR qr, App app) {
        int count = submissionRepository.countSubmissionForQr(qr.getId(), attribute.getRange());
        return new IntegerAttributeValue(attribute, count);
    }

}
