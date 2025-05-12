package com.mryqr.core.qr.infrastructure;

import com.google.common.collect.ImmutableMap;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.indexedfield.IndexedValue;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.qr.domain.AppedQr;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrHouseKeeper;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.QR_NOT_FOUND;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MongoCriteriaUtils.*;
import static com.mryqr.common.utils.MryConstants.QR_COLLECTION;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class MongoQrRepository extends MongoBaseRepository<QR> implements QrRepository {
    private final AppRepository appRepository;
    private final QrHouseKeeper qrHouseKeeper;

    public MongoQrRepository(AppRepository appRepository,
                             QrHouseKeeper qrHouseKeeper) {
        this.appRepository = appRepository;
        this.qrHouseKeeper = qrHouseKeeper;
    }

    //重新计算过滤值和排序值后再保存，一般如果更新了attributeValue，则需要调用该方法
    @Override
    @Transactional
    public void houseKeepSave(QR qr, App app, User user) {
        requireNonNull(qr, "QR must not be null.");
        requireNonNull(app, "App must not be null.");

        qrHouseKeeper.perform(qr, app, user);
        save(qr);
    }

    @Override
    public AppedQr appedQrById(String qrId) {
        requireNonBlank(qrId, "QR ID must not be blank.");

        QR qr = super.byId(qrId);
        App app = appRepository.cachedById(qr.getAppId());
        return new AppedQr(qr, app);
    }

    @Override
    public AppedQr appedQrByIdAndCheckTenantShip(String qrId, User user) {
        requireNonBlank(qrId, "QR ID must not be blank.");

        QR qr = super.byId(qrId);
        checkTenantShip(qr, user);
        App app = appRepository.cachedById(qr.getAppId());
        return new AppedQr(qr, app);
    }

    @Override
    public AppedQr appedQrByCustomIdAndCheckTenantShip(String appId, String customId, User user) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(customId, "Custom ID must not be blank.");

        QR qr = this.byCustomId(appId, customId);
        checkTenantShip(qr, user);
        App app = appRepository.cachedById(qr.getAppId());
        return new AppedQr(qr, app);
    }

    @Override
    public boolean existsByName(String name, String appId) {
        requireNonBlank(name, "Name must not be blank.");
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("appId").is(appId).and("name").is(name));
        return mongoTemplate.exists(query, QR.class);
    }

    @Override
    public Map<String, String> qrNamesOf(Set<String> qrIds) {
        if (isEmpty(qrIds)) {
            return ImmutableMap.of();
        }

        Query query = Query.query(where("_id").in(qrIds));
        query.fields().include("name");

        List<QQrName> qrNames = mongoTemplate.find(query, QQrName.class, QR_COLLECTION);
        return qrNames.stream().collect(toImmutableMap(QQrName::getId, QQrName::getName));
    }

    @Override
    public String qrNameOf(String qrId) {
        requireNonBlank(qrId, "QR ID must not be blank.");

        Query query = Query.query(where("_id").is(qrId));
        query.fields().include("name");
        QQrName qrName = mongoTemplate.findOne(query, QQrName.class, QR_COLLECTION);
        if (qrName == null) {
            throw new MryException(QR_NOT_FOUND, "未找到实例。", mapOf("qrId", qrId));
        }
        return qrName.getName();
    }

    @Override
    public boolean existsByCustomId(String customId, String appId) {
        requireNonBlank(customId, "Custom ID must not be blank.");
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("appId").is(appId).and("customId").is(customId));
        return mongoTemplate.exists(query, QR.class);
    }

    @Override
    public QR byCustomId(String appId, String customId) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(customId, "Custom ID must not be blank.");

        Query query = query(where("appId").is(appId).and("customId").is(customId));
        QR qr = mongoTemplate.findOne(query, QR.class);

        if (qr == null) {
            throw new MryException(QR_NOT_FOUND, "未找到实例。", mapOf("appId", appId, "customId", customId));
        }

        return qr;
    }

    @Override
    public Optional<QR> byCustomIdOptional(String appId, String customId) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(customId, "Custom ID must not be blank.");

        Query query = query(where("appId").is(appId).and("customId").is(customId));
        QR qr = mongoTemplate.findOne(query, QR.class);
        return ofNullable(qr);
    }

    @Override
    public QR byCustomIdAndCheckTenantShip(String appId, String customId, User user) {
        QR qr = byCustomId(appId, customId);
        checkTenantShip(qr, user);
        return qr;
    }

    @Override
    public Optional<QR> byPlateIdOptional(String plateId) {
        requireNonBlank(plateId, "Plate ID must not be blank.");

        Query query = query(where("plateId").is(plateId));
        QR qr = mongoTemplate.findOne(query, QR.class);
        return ofNullable(qr);
    }

    @Override
    public void save(QR it) {
        super.save(it);
    }

    @Override
    public void save(List<QR> qrs) {
        super.save(qrs);
    }

    @Override
    public void insert(List<QR> qrs) {
        super.insert(qrs);
    }

    @Override
    public void delete(QR it) {
        super.delete(it);
    }

    @Override
    public void delete(List<QR> qrs) {
        super.delete(qrs);
    }

    @Override
    public QR byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<QR> byIdOptional(String id) {
        return super.byIdOptional(id);
    }

    @Override
    public QR byIdAndCheckTenantShip(String id, User user) {
        return super.byIdAndCheckTenantShip(id, user);
    }

    @Override
    public List<QR> byIdsAllAndCheckTenantShip(Set<String> ids, User user) {
        return super.byIdsAllAndCheckTenantShip(ids, user);
    }

    @Override
    public List<QR> find(String appId, String startId, int size) {
        requireNonBlank(appId, "App ID must not be blank.");
        requireNonBlank(startId, "Start ID must not be blank.");

        return mongoTemplate.find(query(where("appId").is(appId).and("_id").lt(startId))
                        .with(by(DESC, "_id"))
                        .limit(size),
                QR.class);
    }

    @Override
    public int count(String tenantId) {
        return super.count(tenantId);
    }

    @Override
    public Set<String> assignmentQrIdsOf(String groupId) {
        requireNonBlank(groupId, "Group ID must not be blank.");

        Query query = Query.query(where("groupId").is(groupId).and("active").is(true)).limit(1000).with(by(DESC, "createdAt"));
        return mongoTemplate.findDistinct(query, "_id", QR_COLLECTION, String.class).stream().collect(toImmutableSet());
    }

    @Override
    public void increaseAccessCount(QR qr) {
        requireNonNull(qr, "QR must not be blank.");

        Query query = Query.query(where("_id").is(qr.getId()));
        Update update = new Update();
        update.inc("accessCount");
        update.set("lastAccessedAt", Instant.now());
        mongoTemplate.updateFirst(query, update, QR.class);
    }

    @Override
    public int countQrUnderApp(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = Query.query(where("appId").is(appId));
        return (int) mongoTemplate.count(query, QR.class);
    }

    @Override
    public int removeAllQrsUnderApp(String appId) {
        requireNonBlank(appId, "App ID must not be blank.");

        Query query = query(where("appId").is(appId));
        DeleteResult result = mongoTemplate.remove(query, QR.class);
        return (int) result.getDeletedCount();
    }

    @Override
    public int removeAllQrsUnderGroup(String groupId) {
        requireNonBlank(groupId, "Group ID must not be blank.");

        Query query = Query.query(where("groupId").is(groupId));
        DeleteResult result = mongoTemplate.remove(query, QR.class);
        return (int) result.getDeletedCount();
    }

    @Override
    public int removeAttributeValuesUnderAllQrs(Set<String> attributeIds, String appId) {
        requireNonNull(attributeIds, "Attribute IDs must not be null.");
        requireNonBlank(appId, "APP ID must not be blank.");

        if (isEmpty(attributeIds)) {
            return 0;
        }

        Query query = query(where("appId").is(appId));
        Update update = new Update();
        for (String attributeId : attributeIds) {
            update.unset(mongoAttributeValueFieldOf(attributeId));
        }

        UpdateResult result = mongoTemplate.updateMulti(query, update, QR.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public int removeIndexedOptionUnderAllQrs(String optionId, IndexedField field, String appId) {
        requireNonNull(field, "Field must not be null.");
        requireNonBlank(appId, "APP ID must not be blank.");
        requireNonBlank(optionId, "Option ID must not be blank.");

        Query query = query(where("appId").is(appId));
        Update update = new Update().pull(mongoTextFieldOf(field), optionId);
        UpdateResult result = mongoTemplate.updateMulti(query, update, QR.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public int removeIndexedValueUnderAllQrs(Set<IndexedField> fields, String appId) {
        requireNonNull(fields, "Indexed fields must not be null.");
        requireNonBlank(appId, "App ID must not be blank.");

        if (isEmpty(fields)) {
            return 0;
        }

        Update update = new Update();
        for (IndexedField field : fields) {
            update.unset(mongoIndexedValueFieldOf(field));
        }

        Query query = query(where("appId").is(appId));
        UpdateResult result = mongoTemplate.updateMulti(query, update, QR.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public int removeIndexedValueUnderAllQrs(IndexedField field, String attributeId, String appId) {
        requireNonNull(field, "Indexed field must not be null.");
        requireNonBlank(attributeId, "Attribute ID must not be blank.");
        requireNonBlank(appId, "App ID must not be blank.");

        Update update = new Update().unset(mongoIndexedValueFieldOf(field));

        //由于rid没有建立索引(mongodb有64个索引限制)，因此运行比较慢
        Query query = query(where("appId").is(appId).and(mongoReferencedFieldOf(field)).is(attributeId));
        UpdateResult result = mongoTemplate.updateMulti(query, update, QR.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public int syncGroupActiveStatusToQrs(Group group) {
        requireNonNull(group, "Group must not be null.");

        Query query = Query.query(where("groupId").is(group.getId()));
        Update update = new Update();
        update.set("groupActive", group.isActive());

        UpdateResult result = mongoTemplate.updateMulti(query, update, QR.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public int updateAttributeValueForAllQrsUnderGroup(String groupId, AttributeValue attributeValue) {
        requireNonBlank(groupId, "Group ID must not be null.");
        requireNonNull(attributeValue, "Attribute value must not be null.");

        Update attributeValueUpdate = new Update().set(mongoAttributeValueFieldOf(attributeValue.getAttributeId()), attributeValue);
        UpdateResult result = mongoTemplate.updateMulti(query(where("groupId").is(groupId)), attributeValueUpdate, QR.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public int updateIndexValueForAllQrsUnderGroup(String groupId, IndexedField indexedField, IndexedValue indexedValue) {
        requireNonBlank(groupId, "Group ID must not be null.");
        requireNonNull(indexedField, "Indexed field must not be null.");
        requireNonNull(indexedValue, "Indexed value must not be null.");

        Update indexedValueField = new Update().set(mongoIndexedValueFieldOf(indexedField), indexedValue);
        UpdateResult result = mongoTemplate.updateMulti(query(where("groupId").is(groupId)), indexedValueField, QR.class);
        return (int) result.getModifiedCount();
    }

    @Override
    public Optional<QR> latestQrForTenant(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be null.");

        Query query = query(where("tenantId").is(tenantId)).with(by(DESC, "createdAt"));
        QR qr = mongoTemplate.findOne(query, QR.class);
        return Optional.ofNullable(qr);
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class QQrName {
        private final String id;
        private final String name;
    }
}
