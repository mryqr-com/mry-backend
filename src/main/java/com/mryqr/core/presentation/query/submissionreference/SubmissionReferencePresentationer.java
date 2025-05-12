package com.mryqr.core.presentation.query.submissionreference;

import com.google.common.collect.ImmutableMap;
import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PSubmissionReferenceControl;
import com.mryqr.core.member.domain.MemberAware;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.presentation.query.ControlPresentationer;
import com.mryqr.core.presentation.query.QControlPresentation;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.app.domain.page.control.ControlType.SUBMISSION_REFERENCE;
import static java.util.function.Function.identity;

@Component
@RequiredArgsConstructor
public class SubmissionReferencePresentationer implements ControlPresentationer {
    private final SubmissionRepository submissionRepository;
    private final MemberRepository memberRepository;

    @Override
    public boolean canHandle(Control control) {
        return control.getType() == SUBMISSION_REFERENCE;
    }

    @Override
    public QControlPresentation present(QR qr, Control control, App app) {
        PSubmissionReferenceControl theControl = (PSubmissionReferenceControl) control;

        return submissionRepository.lastInstanceSubmission(qr.getId(), theControl.getPageId())
                .map(submission -> {
                    Set<String> allMemberIds = submission.allAnswers().values().stream()
                            .filter(answer -> answer instanceof MemberAware)
                            .map(answer -> ((MemberAware) answer).awaredMemberIds())
                            .flatMap(Collection::stream)
                            .collect(toImmutableSet());

                    Map<String, MemberReference> memberReferences = memberRepository.cachedMemberReferences(app.getTenantId(), allMemberIds);
                    SubmissionReferenceContext referenceContext = SubmissionReferenceContext.builder()
                            .memberReferences(memberReferences)
                            .build();

                    Map<String, DisplayValue> values = submission.allAnswers().values().stream()
                            .map(answer -> answer.toDisplayValue(referenceContext))
                            .filter(Objects::nonNull)
                            .collect(toImmutableMap(DisplayValue::getKey, identity()));

                    return new QSubmissionReferencePresentation(values);
                }).orElse(new QSubmissionReferencePresentation(ImmutableMap.of()));
    }
}
