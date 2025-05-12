package com.mryqr.core.qr.domain.attribute.sync.calculator;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;

@Component
@RequiredArgsConstructor
public class LastSubmissionAnswerRefCalculator implements AttributeValueCalculator {
    private final SubmissionRepository submissionRepository;

    @Override
    public boolean canCalculate(Attribute attribute, App app) {
        return attribute.getType() == CONTROL_LAST;
    }

    @Override
    public AttributeValue calculate(Attribute attribute, QR qr, App app) {
        return app.controlByIdOptional(attribute.getControlId())
                .flatMap(control -> submissionRepository.lastInstanceSubmission(qr.getId(), attribute.getPageId())
                        .flatMap(submission -> submission.answerForControlOptional(attribute.getControlId())
                                .map(answer -> answer.toAttributeValue(attribute, control))))
                .orElse(null);
    }
}
