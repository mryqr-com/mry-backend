package com.mryqr.core.submission.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeStatisticRange;
import com.mryqr.core.common.domain.indexedfield.IndexedField;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.qr.domain.QR;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SubmissionRepository {
    void houseKeepSave(Submission submission, App app);

    Optional<Submission> lastMemberSubmission(String memberId, String qrId, String pageId);

    Optional<Submission> lastInstanceSubmission(String qrId, String pageId);

    Optional<Submission> firstInstanceSubmission(String qrId, String pageId);

    boolean alreadyExistsForAnswerUnderApp(String answerValue,
                                           IndexedField indexedField,
                                           String appId,
                                           String pageId,
                                           String selfSubmissionId);

    boolean alreadyExistsForAnswerUnderQr(String answerValue,
                                          IndexedField indexedField,
                                          String pageId,
                                          String qrId,
                                          String selfSubmissionId);

    void save(Submission it);

    void insert(List<Submission> submissions);

    void delete(Submission it);

    Submission byId(String id);

    Optional<Submission> byIdOptional(String id);

    Submission byIdAndCheckTenantShip(String id, User user);

    int count(String tenantId);

    int countSubmissionForQr(String qrId, AttributeStatisticRange range);

    int countPageSubmissionForQr(String pageId, String qrId, AttributeStatisticRange range);

    Double calculateStatisticValueForQr(Attribute attribute, QR qr, App app);

    int countSubmissionForApp(String appId);

    int removeAllSubmissionForApp(String appId);

    int removeAllSubmissionForGroup(String groupId);

    int removeAllSubmissionForPage(String pageId, String appId);

    int removeAllSubmissionForQr(String qrId);

    int removeControlAnswersFromAllSubmissions(Set<String> controlIds, String appId);

    int removeIndexedValueFromAllSubmissions(IndexedField field, String pageId, String appId);

    int removeIndexedValueFromAllSubmissions(IndexedField field, String controlId, String pageId, String appId);

    int removeIndexedOptionFromAllSubmissions(String optionId, IndexedField indexedField, String pageId, String appId);

    int syncGroupFromQr(QR qr);

    int syncPlateFromQr(QR qr);

    Optional<Submission> latestQrForTenant(String tenantId);

}
