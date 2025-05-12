package com.mryqr.core.presentation.query.submithistory;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PSubmitHistoryControl;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.member.domain.MemberAware;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.presentation.query.ControlPresentationer;
import com.mryqr.core.presentation.query.QControlPresentation;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.SubmissionApproval;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.app.domain.page.control.ControlType.SUBMIT_HISTORY;
import static com.mryqr.core.common.utils.MryConstants.SUBMISSION_COLLECTION;
import static com.mryqr.core.submission.domain.ApprovalStatus.statusOf;
import static java.util.function.Function.identity;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class SubmitHistoryPresentationer implements ControlPresentationer {
    private final MongoTemplate mongoTemplate;
    private final MemberRepository memberRepository;

    @Override
    public boolean canHandle(Control control) {
        return control.getType() == SUBMIT_HISTORY;
    }

    @Override
    public QControlPresentation present(QR qr, Control control, App app) {
        PSubmitHistoryControl theControl = (PSubmitHistoryControl) control;

        Query query = Query.query(where("qrId").is(qr.getId()).and("pageId").in(theControl.getPageIds()));
        query.with(by(theControl.isOrderByAsc() ? ASC : DESC, "createdAt")).limit(theControl.getMax());
        query.fields().include("pageId").include("answers").include("approval")
                .include("createdBy").include("creator").include("createdAt");

        List<SubmitHistoryDto> submissionDtos = mongoTemplate.find(query, SubmitHistoryDto.class, SUBMISSION_COLLECTION);
        if (isEmpty(submissionDtos)) {
            return new QSubmitHistoryPresentation(List.of());
        }

        Set<String> summaryEligibleControlIds = submissionDtos.stream()
                .map(SubmitHistoryDto::getPageId)
                .map(pageId -> app.pageByIdOptional(pageId).orElse(null))
                .filter(Objects::nonNull)
                .map(Page::submissionSummaryEligibleControlIds)
                .flatMap(Collection::stream)
                .collect(toImmutableSet());

        submissionDtos.forEach(dto -> dto.answers.entrySet()
                .removeIf(entry -> !summaryEligibleControlIds.contains(entry.getKey())));

        Set<String> allMemberIds = submissionDtos.stream()
                .map(SubmitHistoryDto::referencedMemberIds)
                .flatMap(Collection::stream)
                .collect(toImmutableSet());

        Map<String, MemberReference> memberReferences = memberRepository.cachedMemberReferences(app.getTenantId(), allMemberIds);
        SubmissionReferenceContext referenceContext = SubmissionReferenceContext.builder()
                .memberReferences(memberReferences)
                .build();

        List<QSubmitHistorySubmission> submissions = submissionDtos.stream()
                .map(dto -> {
                    Map<String, DisplayValue> displayValues = dto.answers.values().stream()
                            .map(answer -> answer.toDisplayValue(referenceContext))
                            .filter(Objects::nonNull)
                            .collect(toImmutableMap(DisplayValue::getKey, identity()));

                    return QSubmitHistorySubmission.builder()
                            .id(dto.id)
                            .pageId(dto.pageId)
                            .values(displayValues)
                            .approvalStatus(statusOf(dto.getApproval()))
                            .createdBy(dto.getCreatedBy())
                            .creator(dto.getCreator())
                            .createdAt(dto.getCreatedAt())
                            .build();
                }).collect(toImmutableList());

        return new QSubmitHistoryPresentation(submissions);
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    static class SubmitHistoryDto {
        private final String id;
        private final String pageId;
        private final Map<String, Answer> answers;
        private final SubmissionApproval approval;
        private final String createdBy;
        private final String creator;
        private final Instant createdAt;

        public Set<String> referencedMemberIds() {
            return answers.values().stream()
                    .filter(answer -> answer instanceof MemberAware)
                    .map(answer -> ((MemberAware) answer).awaredMemberIds())
                    .flatMap(Collection::stream)
                    .collect(toImmutableSet());
        }
    }
}
