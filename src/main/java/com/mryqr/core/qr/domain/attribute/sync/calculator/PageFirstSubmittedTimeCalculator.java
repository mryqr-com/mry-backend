package com.mryqr.core.qr.domain.attribute.sync.calculator;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.TimestampAttributeValue;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.PAGE_FIRST_SUBMITTED_TIME;

@Component
@RequiredArgsConstructor
public class PageFirstSubmittedTimeCalculator implements AttributeValueCalculator {
    private final SubmissionRepository submissionRepository;

    @Override
    public boolean canCalculate(Attribute attribute, App app) {
        return attribute.getType() == PAGE_FIRST_SUBMITTED_TIME;
    }

    @Override
    public AttributeValue calculate(Attribute attribute, QR qr, App app) {
        return submissionRepository.firstInstanceSubmission(qr.getId(), attribute.getPageId())
                .map(submission -> new TimestampAttributeValue(attribute, submission.getCreatedAt()))
                .orElse(null);
    }
}
