package com.mryqr.core.submission.domain;

import com.mryqr.common.domain.AggregateRoot;
import com.mryqr.common.domain.indexedfield.IndexedValues;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.event.SubmissionApprovedEvent;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import com.mryqr.core.submission.domain.event.SubmissionDeletedEvent;
import com.mryqr.core.submission.domain.event.SubmissionUpdatedEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.SUBMISSION_ALREADY_APPROVED;
import static com.mryqr.common.exception.ErrorCode.SUBMISSION_NOT_ALLOWED_BY_CIRCULATION;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.SUBMISSION_COLLECTION;
import static com.mryqr.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static java.time.Instant.now;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Getter
@Document(SUBMISSION_COLLECTION)
@TypeAlias(SUBMISSION_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class Submission extends AggregateRoot {
    private String qrId;//提交对应的QR
    private String plateId;//对应QR的plateId,用于前端打开某submission
    private String groupId;//提交对应的QR对应的Group
    private String appId;//提交对应的App
    private String pageId;//提交对应的Page
    private Map<String, Answer> answers;//答案，controlId -> Answer
    private SubmissionApproval approval;//审批信息
    private String ip;//提交时的IP地址
    private String referenceData;

    private IndexedValues ivs;//索引值，index values的缩写
    private Set<String> svs; //可搜索值，search values的缩写

    public Submission(Map<String, Answer> answers, String pageId, QR qr, App app, String referenceData, User user) {
        super(newSubmissionId(), qr.getTenantId(), user);
        if (!app.canSubmitByCirculationStatus(qr.getCirculationOptionId(), pageId)) {
            throw new MryException(SUBMISSION_NOT_ALLOWED_BY_CIRCULATION,
                    "提交失败，当前流转状态不允许提交该表单。",
                    mapOf("qrId", qr.getId(), "pageId", pageId, "circulation", qr.getCirculationOptionId()));
        }

        this.qrId = qr.getId();
        this.plateId = qr.getPlateId();
        this.groupId = qr.getGroupId();
        this.appId = qr.getAppId();
        this.pageId = pageId;
        this.answers = answers;
        this.referenceData = referenceData;
        raiseEvent(new SubmissionCreatedEvent(this.getId(), this.qrId, this.appId, this.pageId, user));
        this.addOpsLog("新建", user);
    }

    public static String newSubmissionId() {
        return "SBM" + newSnowflakeId();
    }

    public void update(Set<String> submittedControlIds, Map<String, Answer> checkedAnswers, User user) {
        //需要合并，因为对于autoCalculate的控件有可能请求不提供answer但是后端依然自动产生answer
        Set<String> providedAnswerControlIds = Stream.of(checkedAnswers.keySet(), submittedControlIds)
                .flatMap(Collection::stream).collect(toImmutableSet());

        Map<String, Answer> existingAnswers = this.answers.values().stream()//本次提交未提供新answer的已有answer
                .filter(answer -> !providedAnswerControlIds.contains(answer.getControlId()))
                .collect(toImmutableMap(Answer::getControlId, identity()));

        Map<String, Answer> finalAnswers = Stream.of(checkedAnswers, existingAnswers)//合并
                .flatMap(m -> m.entrySet().stream())
                .collect(toImmutableMap(Entry::getKey, Entry::getValue));

        if (!Objects.equals(this.answers, finalAnswers)) {
            raiseEvent(new SubmissionUpdatedEvent(this.getId(), this.qrId, this.appId, this.pageId, user));
        }

        this.answers = finalAnswers;
        addOpsLog("更新提交", user);
    }

    public void approve(boolean passed, String note, Page page, User user) {
        if (isApproved()) {
            throw new MryException(SUBMISSION_ALREADY_APPROVED, "无法完成审批，先前已经完成审批。", "submissionId", this.getId());
        }

        this.approval = SubmissionApproval.builder().passed(passed).note(note).approvedAt(now()).approvedBy(user.getMemberId()).build();
        raiseEvent(new SubmissionApprovedEvent(this.getId(), this.getQrId(), this.getAppId(), this.getPageId(), this.approval, user));
        addOpsLog(passed ? "审批" + page.approvalPassText() : "审批" + page.approvalNotPassText(), user);
    }

    public boolean isApproved() {
        return approval != null;
    }

    public void setIndexedValues(IndexedValues values) {
        if (values == null || values.isEmpty()) {
            this.ivs = null;
        } else {
            this.ivs = values;
        }
    }

    public IndexedValues getIndexedValues() {
        return ivs;
    }

    public void setSearchableValues(Set<String> values) {
        this.svs = isNotEmpty(values) ? values : null;
    }

    public Set<String> getSearchableValues() {
        return svs;
    }

    public Optional<Answer> answerForControlOptional(String controlId) {
        return ofNullable(answers.get(controlId));
    }

    public Map<String, Answer> allAnswers() {
        return answers;
    }

    public void cleanAnswers(App app) {
        Map<String, Control> controlMap = app.allPages().stream()
                .map(Page::getControls).flatMap(Collection::stream)
                .collect(toImmutableMap(Control::getId, identity()));

        //保证只有control存在时，answer才能持久化
        List<Answer> hasControlAnswers = this.answers.values().stream()
                .filter(answer -> controlMap.containsKey(answer.getControlId()))
                .collect(toImmutableList());

        //对每个answer进行清洗
        hasControlAnswers.forEach(answer -> answer.clean(controlMap.get(answer.getControlId())));

        //清洗完后，保证只有有值的answer才留下来
        this.answers = hasControlAnswers.stream().filter(Answer::isFilled).collect(toImmutableMap(Answer::getControlId, identity()));
    }

    public boolean hasNoAnswers() {
        return this.answers.isEmpty();
    }

    public void onDelete(User user) {
        raiseEvent(new SubmissionDeletedEvent(this.getId(), this.getQrId(), this.getAppId(), this.getPageId(), user));
    }
}
