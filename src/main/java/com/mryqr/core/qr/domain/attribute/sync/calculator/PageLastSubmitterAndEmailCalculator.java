package com.mryqr.core.qr.domain.attribute.sync.calculator;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.MemberEmailAttributeValue;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.PAGE_LAST_SUBMITTER_AND_EMAIL;

@Component
@RequiredArgsConstructor
public class PageLastSubmitterAndEmailCalculator implements AttributeValueCalculator {
    private final SubmissionRepository submissionRepository;

    @Override
    public boolean canCalculate(Attribute attribute, App app) {
        return attribute.getType() == PAGE_LAST_SUBMITTER_AND_EMAIL;
    }

    @Override
    public AttributeValue calculate(Attribute attribute, QR qr, App app) {
        return submissionRepository.lastInstanceSubmission(qr.getId(), attribute.getPageId())
                .map(submission -> new MemberEmailAttributeValue(attribute, submission.getCreatedBy()))
                .orElse(null);
    }
}
