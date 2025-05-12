package com.mryqr.core.submission.infrastructure;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeStatisticRange;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionHouseKeeper;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MongoCriteriaUtils.*;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.startAt;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class MongoSubmissionRepository extends MongoBaseRepository<Submission> implements SubmissionRepository {
    private final SubmissionHouseKeeper submissionHouseKeeper;

    public MongoSubmissionRepository(SubmissionHouseKeeper submissionHouseKeeper) {
        this.submissionHouseKeeper = submissionHouseKeeper;
    }

    //重新计算过滤值和排序值后再保存，一般如果新建或更新了answer，则需要调用该方法
    @Override
    @Transactional
    public void houseKeepSave(Submission submission, App app) {
        submissionHouseKeeper.perform(submission, app);
        save(submission);
    }

    @Override
    public Optional<Submission> lastMemberSubmission(String memberId, String qrId, String pageId) {
        requireNonBlank(memberId, "Member ID must not be blank.");
        requireNonBlank(qrId, "QR ID must not be blank.");
        requireNonBlank(pageId, "Page ID must not be blank.");

        Query query = query(where("qrId").is(qrId)
                .and("pageId").is(pageId)
                .and("createdBy").is(memberId))
                .with(by(DESC, "createdAt"));
        return ofNullable(mongoTemplate.findOne(query, Submission.class));
    }

    @Override
    public Optional<Submission> lastInstanceSubmission(String qrId, String pageId) {
        requireNonBlank(qrId, "QR ID must not be blank.");
        requireNonBlank(pageId, "Page ID must not be blank.");

        Query query = query(where("qrId").is(qrId).and("pageId").is(pageId)).with(by(DESC, "createdAt"));
        return ofNullable(mongoTemplate.findOne(query, Submission.class));
    }

    @Override
    public Optional<Submission> firstInstanceSubmission(String qrId, String pageId) {
        requireNonBlank(qrId, "QR ID must not be blank.");
        requireNonBlank(pageId, "Page ID must not be blank.");

        Query query = query(where("qrId").is(qrId).and("pageId").is(pageId)).with(by(ASC, "createdAt"));
        return ofNullable(mongoTemplate.findOne(query, Submission.class));
    }

    @Override
    public boolean alreadyExistsForAnswerUnderApp(String answerValue,
                                                  IndexedField indexedField,
                                                  String appId,
                                                  String pageId,
                                                  String selfSubmissionId) {
        requireNonBlank(answerValue, "Answer value must not be blank.");
        requireNonNull(indexedField, "Indexed field must not be null.");
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(pageId, "Page ID must not be blank.");

        Query query = query(where("appId").is(appId)
                .and("pageId").is(pageId)
                .and(mongoTextFieldOf(indexedField)).is(answerValue));

        if (isNotBlank(selfSubmissionId)) {
            query.addCriteria(where("_id").ne(selfSubmissionId));//对于更新，检查时不包含自身
        }

        return mongoTemplate.exists(query, Submission.class);
    }

    @Override
    public boolean alreadyExistsForAnswerUnderQr(String answerValue,
                                                 IndexedField indexedField,
                                                 String pageId,
                                                 String qrId,
                                                 String selfSubmissionId) {
        requireNonBlank(answerValue, "Answer value must not be blank.");
        requireNonNull(indexedField, "Indexed field must not be null.");
        requireNonBlank(pageId, "Page ID must not be blank.");
        requireNonBlank(qrId, "QR ID must not be blank.");

        Query query = query(where("qrId").is(qrId)
                .and("pageId").is(pageId)
                .and(mongoTextFieldOf(indexedField)).is(answerValue));

        if (isNotBlank(selfSubmissionId)) {
            query.addCriteria(where("_id").ne(selfSubmissionId));//对于更新，检查时不包含自身
        }

        return mongoTemplate.exists(query, Submission.class);
    }

    @Override
    public void save(Submission it) {
        super.save(it);
    }

    @Override
    public void insert(List<Submission> submissions) {
        super.insert(submissions);
    }

    @Override
    public void delete(Submission it) {
        super.delete(it);
    }

    @Override
    public Submission byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<Submission> byIdOptional(String id) {
        return super.byIdOptional(id);
    }

    @Override
    public Submission byIdAndCheckTenantShip(String id, User user) {
        return super.byIdAndCheckTenantShip(id, user);
    }

    @Override
    public int count(String tenantId) {
        return super.count(tenantId);
    }

    @Override
    public int countSubmissionForQr(String qrId, AttributeStatisticRange range) {
        requireNonBlank(qrId, "QR ID must not be blank.");
        requireNonNull(range, "Range must not be null.");

        Query query = query(where("qrId").is(qrId));
        startAt(range).ifPresent(start -> query.addCriteria(where("createdAt").gte(start)));
        return (int) mongoTemplate.count(query, Submission.class);
    }

    @Override
    public int countPageSubmissionForQr(String pageId, String qrId, AttributeStatisticRange range) {
        requireNonBlank(pageId, "Page ID must not be blank.");
        requireNonBlank(qrId, "QR ID must not be blank.");
        requireNonNull(range, "Range must not be null.");

        Query query = query(where("qrId").is(qrId).and("pageId").is(pageId));
        startAt(range).ifPresent(start -> query.addCriteria(where("createdAt").gte(start)));
        return (int) mongoTemplate.count(query, Submission.class);
    }

    @Override
    public Double calculateStatisticValueForQr(Attribute attribute, QR qr, App app) {
        requireNonNull(attribute, "Attribute must not be null.");
        requireNonNull(qr, "QR must not be null.");
        requireNonNull(app, "App must not be null.");

        String qrId = qr.getId();
        String pageId = attribute.getPageId();
        String controlId = attribute.getControlId();
        requireNonBlank(pageId, "Page ID must not be blank.");
        requireNonBlank(controlId, "Control ID must not be blank.");


        IndexedField indexedField = app.indexedFieldForControl(pageId, controlId);
        Criteria criteria = where("qrId").is(qrId).and("pageId").is(pageId);
        Optional<Instant> rangeStartAt = startAt(attribute.getRange());
        if (rangeStartAt.isPresent()) {
            criteria = criteria.and("createdAt").gte(rangeStartAt.get());
        }

        Aggregation aggregation = newAggregation(
                match(criteria),
                groupOperation(attribute, indexedField)
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        Result result = mongoTemplate.aggregate(aggregation, Submission.class, Result.class).getUniqueMappedResult();

        if (result == null || result.getResult() == null) {
            return null;
        }

        return result.getResult();
    }

    @Override
    public int countSubmissionForApp(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = Query.query(where("appId").is(appId));
        return (int) mongoTemplate.count(query, Submission.class);
    }

    @Override
    public int removeAllSubmissionForApp(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("appId").is(appId));
        DeleteResult result = mongoTemplate.remove(query, Submission.class);
        return (int) result.getDeletedCount();
    }

    @Override
    public int removeAllSubmissionForGroup(String groupId) {
        requireNonBlank(groupId, "Group ID must not be blank.");

        Query query = Query.query(where("groupId").is(groupId));
        DeleteResult result = mongoTemplate.remove(query, Submission.class);
        return (int) result.getDeletedCount();
    }

    @Override
    public int removeAllSubmissionForPage(String pageId, String appId) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(pageId, "Page ID must not be blank.");

        Query query = Query.query(where("appId").is(appId).and("pageId").is(pageId));
        DeleteResult result = mongoTemplate.remove(query, Submission.class);
        return (int) result.getDeletedCount();
    }

    @Override
    public int removeAllSubmissionForQr(String qrId) {
        requireNonBlank(qrId, "QR ID must not be blank.");

        Query query = query(where("qrId").is(qrId));
        DeleteResult result = mongoTemplate.remove(query, Submission.class);
        return (int) result.getDeletedCount();
    }

    @Override
    public int removeControlAnswersFromAllSubmissions(Set<String> controlIds, String appId) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonNull(controlIds, "Control IDs must not be null.");

        if (isEmpty(controlIds)) {
            return 0;
        }

        Update update = new Update();
        for (String controlId : controlIds) {
            update.unset(mongoAnswerFieldOf(controlId));
        }

        UpdateResult result = mongoTemplate.updateMulti(query(where("appId").is(appId)), update, Submission.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public int removeIndexedValueFromAllSubmissions(IndexedField field, String pageId, String appId) {
        requireNonNull(field, "Field must not be null.");
        requireNonBlank(pageId, "Page ID must not be blank.");
        requireNonBlank(appId, "App ID must not be blank.");

        Update update = new Update().unset(mongoIndexedValueFieldOf(field));
        Query query = query(where("appId").is(appId).and("pageId").is(pageId));
        UpdateResult result = mongoTemplate.updateMulti(query, update, Submission.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public int removeIndexedValueFromAllSubmissions(IndexedField field, String controlId, String pageId, String appId) {
        requireNonNull(field, "Field must not be null.");
        requireNonBlank(controlId, "Control ID must not be blank.");
        requireNonBlank(pageId, "Page ID must not be blank.");
        requireNonBlank(appId, "App ID must not be blank.");

        Update update = new Update().unset(mongoIndexedValueFieldOf(field));
        Query query = query(where("appId").is(appId)
                .and("pageId").is(pageId)
                .and(mongoReferencedFieldOf(field)).is(controlId));//由于rid没有建立索引，因此运行比较慢
        UpdateResult result = mongoTemplate.updateMulti(query, update, Submission.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public int removeIndexedOptionFromAllSubmissions(String optionId, IndexedField indexedField, String pageId, String appId) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(pageId, "Page ID must not be blank.");
        requireNonBlank(optionId, "Option ID must not be blank.");

        Query query = query(where("appId").is(appId).and("pageId").is(pageId));
        Update update = new Update().pull(mongoTextFieldOf(indexedField), optionId);
        UpdateResult result = mongoTemplate.updateMulti(query, update, Submission.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public int syncGroupFromQr(QR qr) {
        requireNonNull(qr, "QR must not be null.");

        Query query = query(where("qrId").is(qr.getId()));
        Update update = new Update();
        update.set("groupId", qr.getGroupId());
        UpdateResult result = mongoTemplate.updateMulti(query, update, Submission.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public int syncPlateFromQr(QR qr) {
        requireNonNull(qr, "QR must not be null.");

        Query query = query(where("qrId").is(qr.getId()));
        Update update = new Update();
        update.set("plateId", qr.getPlateId());
        UpdateResult result = mongoTemplate.updateMulti(query, update, Submission.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public Optional<Submission> latestSubmissionForTenant(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be null.");

        Query query = query(where("tenantId").is(tenantId)).with(by(DESC, "createdAt"));
        Submission submission = mongoTemplate.findOne(query, Submission.class);
        return Optional.ofNullable(submission);
    }

    private GroupOperation groupOperation(Attribute attribute, IndexedField field) {
        String fieldName = "$" + mongoSortableFieldOf(field);
        switch (attribute.getType()) {
            case CONTROL_SUM -> {
                return group().sum(fieldName).as("result");
            }
            case CONTROL_AVERAGE -> {
                return group().avg(fieldName).as("result");
            }
            case CONTROL_MAX -> {
                return group().max(fieldName).as("result");
            }
            case CONTROL_MIN -> {
                return group().min(fieldName).as("result");
            }
        }
        throw new IllegalStateException("Attribute type[" + attribute.getType() + "] not supported.");
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    private static class Result {
        Double result;
    }
}
