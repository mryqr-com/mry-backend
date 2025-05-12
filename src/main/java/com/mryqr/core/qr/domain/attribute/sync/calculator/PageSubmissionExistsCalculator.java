package com.mryqr.core.qr.domain.attribute.sync.calculator;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.BooleanAttributeValue;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.mryqr.core.app.domain.attribute.AttributeType.PAGE_SUBMISSION_EXISTS;

@Component
@RequiredArgsConstructor
public class PageSubmissionExistsCalculator implements AttributeValueCalculator {
    private final SubmissionRepository submissionRepository;

    @Override
    public boolean canCalculate(Attribute attribute, App app) {
        return attribute.getType() == PAGE_SUBMISSION_EXISTS;
    }

    @Override
    public AttributeValue calculate(Attribute attribute, QR qr, App app) {
        Optional<Submission> submission = submissionRepository.lastInstanceSubmission(qr.getId(), attribute.getPageId());
        return new BooleanAttributeValue(attribute, submission.isPresent());
    }
}
