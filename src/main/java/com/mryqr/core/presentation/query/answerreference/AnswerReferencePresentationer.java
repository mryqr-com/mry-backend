package com.mryqr.core.presentation.query.answerreference;

import com.google.common.collect.ImmutableMap;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PAnswerReferenceControl;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.member.domain.MemberAware;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.presentation.query.ControlPresentationer;
import com.mryqr.core.presentation.query.QControlPresentation;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.mryqr.core.app.domain.page.control.ControlType.ANSWER_REFERENCE;

@Component
@RequiredArgsConstructor
public class AnswerReferencePresentationer implements ControlPresentationer {
    private final SubmissionRepository submissionRepository;
    private final MemberRepository memberRepository;

    @Override
    public boolean canHandle(Control control) {
        return control.getType() == ANSWER_REFERENCE;
    }

    @Override
    public QControlPresentation present(QR qr, Control control, App app) {
        PAnswerReferenceControl theControl = (PAnswerReferenceControl) control;

        return submissionRepository.lastInstanceSubmission(qr.getId(), theControl.getPageId())
                .map(submission -> {
                    Answer answer = submission.allAnswers().get(theControl.getControlId());
                    if (answer == null) {
                        return new QAnswerReferencePresentation(null);
                    }

                    Map<String, MemberReference> memberReferences = answer instanceof MemberAware memberAware ?
                            memberRepository.cachedMemberReferences(app.getTenantId(), memberAware.awaredMemberIds()) : ImmutableMap.of();

                    SubmissionReferenceContext referenceContext = SubmissionReferenceContext.builder()
                            .memberReferences(memberReferences)
                            .build();

                    DisplayValue displayValue = answer.toDisplayValue(referenceContext);
                    return new QAnswerReferencePresentation(displayValue);
                }).orElse(new QAnswerReferencePresentation(null));
    }
}
