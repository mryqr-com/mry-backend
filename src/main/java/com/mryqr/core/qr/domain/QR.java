package com.mryqr.core.qr.domain;

import com.google.common.collect.ImmutableMap;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.common.domain.AggregateRoot;
import com.mryqr.core.common.domain.Geolocation;
import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.common.domain.indexedfield.IndexedValues;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.event.QrActivatedEvent;
import com.mryqr.core.qr.domain.event.QrAttributesUpdatedEvent;
import com.mryqr.core.qr.domain.event.QrBaseSettingUpdatedEvent;
import com.mryqr.core.qr.domain.event.QrCirculationStatusChangedEvent;
import com.mryqr.core.qr.domain.event.QrCustomIdUpdatedEvent;
import com.mryqr.core.qr.domain.event.QrDeactivatedEvent;
import com.mryqr.core.qr.domain.event.QrDeletedEvent;
import com.mryqr.core.qr.domain.event.QrDescriptionUpdatedEvent;
import com.mryqr.core.qr.domain.event.QrGeolocationUpdatedEvent;
import com.mryqr.core.qr.domain.event.QrGroupChangedEvent;
import com.mryqr.core.qr.domain.event.QrHeaderImageUpdatedEvent;
import com.mryqr.core.qr.domain.event.QrMarkedAsTemplateEvent;
import com.mryqr.core.qr.domain.event.QrPlateResetEvent;
import com.mryqr.core.qr.domain.event.QrRenamedEvent;
import com.mryqr.core.qr.domain.event.QrUnMarkedAsTemplateEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_NOT_ACTIVE;
import static com.mryqr.core.common.exception.ErrorCode.QR_NOT_ACTIVE;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.MryConstants.QR_COLLECTION;
import static com.mryqr.core.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static com.mryqr.core.plate.domain.Plate.newPlateId;
import static java.time.Instant.now;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.MapUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.right;

@Getter
@Document(QR_COLLECTION)
@TypeAlias(QR_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class QR extends AggregateRoot {
    private String name;//名称
    private String plateId;//对应码牌ID，每个QR都有一个对应的plateId
    private String appId;//对应App的ID
    private String groupId;//所在组，一个QR必须位于某个组下
    private boolean template;//是否为模板
    private UploadedFile headerImage;//自定义首页页眉图片
    private String description;//自定义描述
    private Map<String, AttributeValue> attributeValues;//自定义属性值,attributeId -> attributeValue
    private int accessCount;//访问次数
    private Instant lastAccessedAt;//上次访问时间
    private Geolocation geolocation;//定位
    private String customId;//自定义编号，用于API查询用，在整个App下唯一
    private boolean active;//是否启用
    private boolean groupActive;//所在group是否启用，通过EDA完成更新
    private String circulationOptionId;//流转状态

    private Set<String> text;//由name分词而来，用于检索但非全文检索
    private IndexedValues ivs;//索引值，index values的缩写
    private Set<String> svs; //可搜索值，search values的缩写

    //正常创建
    public QR(String name, String groupId, App app, String customId, User user) {
        super(newQrId(), app.getTenantId(), user);
        setName(name);
        this.plateId = newPlateId();
        this.appId = app.getId();
        this.groupId = groupId;
        this.customId = customId;
        this.active = true;
        this.groupActive = true;
        this.attributeValues = ImmutableMap.of();
        this.circulationOptionId = app.circulationInitOptionId();
        raiseEvent(new QrCreatedEvent(this.getId(), plateId, groupId, appId, user));
        addOpsLog("新建", user);
    }

    //指定plateId创建
    public QR(String name, String plateId, String groupId, App app, User user) {
        super(newQrId(), app.getTenantId(), user);
        setName(name);
        this.plateId = plateId;
        this.appId = app.getId();
        this.groupId = groupId;
        this.active = true;
        this.groupActive = true;
        this.attributeValues = ImmutableMap.of();
        this.circulationOptionId = app.circulationInitOptionId();
        raiseEvent(new QrCreatedEvent(this.getId(), plateId, groupId, appId, user));
        addOpsLog("新建", user);
    }

    //从已有qr模板创建
    public QR(QR templateQr, App app, User user) {
        super(newQrId(), templateQr.getTenantId(), user);
        setName(String.format("%s(%s)", templateQr.getName(), right(this.getId(), 4)));
        this.plateId = newPlateId();
        this.appId = templateQr.getAppId();
        this.groupId = templateQr.getGroupId();
        this.active = true;
        this.groupActive = templateQr.isGroupActive();
        this.attributeValues = ImmutableMap.of();
        this.circulationOptionId = app.circulationInitOptionId();
        raiseEvent(new QrCreatedEvent(this.getId(), plateId, groupId, appId, user));
        addOpsLog("从模板新建", user);
    }

    //从已有码牌创建
    public QR(String name, String groupId, Plate plate, App app, User user) {
        super(newQrId(), plate.getTenantId(), user);
        setName(name);
        this.plateId = plate.getId();
        this.appId = plate.getAppId();
        this.groupId = groupId;
        this.active = true;
        this.groupActive = true;
        this.attributeValues = ImmutableMap.of();
        this.circulationOptionId = app.circulationInitOptionId();
        raiseEvent(new QrCreatedEvent(this.getId(), plateId, groupId, appId, user));
        addOpsLog("扫码新建", user);
    }

    public static String newQrId() {
        return "QRC" + newSnowflakeId();
    }

    public void rename(String name, User user) {
        if (Objects.equals(this.name, name)) {
            return;
        }

        setName(name);
        raiseEvent(new QrRenamedEvent(this.getId(), appId, name, user));
        addOpsLog("重命名为[" + name + "]", user);
    }

    public void changeGroup(String newGroupId, User user) {
        if (Objects.equals(this.groupId, newGroupId)) {
            return;
        }

        String oldGroupId = this.groupId;
        this.groupId = newGroupId;
        raiseEvent(new QrGroupChangedEvent(this.getId(), appId, oldGroupId, newGroupId, user));
        addOpsLog("转移到[" + newGroupId + "]", user);
    }

    public void resetPlate(String newPlateId, User user) {
        if (Objects.equals(this.plateId, newPlateId)) {
            return;
        }

        String oldPlateId = this.plateId;
        this.plateId = newPlateId;
        raiseEvent(new QrPlateResetEvent(this.getId(), appId, oldPlateId, newPlateId, user));
        addOpsLog("重置码牌为[" + newPlateId + "]", user);
    }

    public void markAsTemplate(User user) {
        if (this.template) {
            return;
        }

        this.template = true;
        addOpsLog("设为模板", user);
        raiseEvent(new QrMarkedAsTemplateEvent(this.getId(), appId, user));
    }

    public void unmarkAsTemplate(User user) {
        if (!this.template) {
            return;
        }

        this.template = false;
        addOpsLog("解设模板", user);
        raiseEvent(new QrUnMarkedAsTemplateEvent(this.getId(), appId, user));
    }

    public void updateBaseSetting(String name,
                                  String description,
                                  UploadedFile headerImage,
                                  Map<String, AttributeValue> directAttributeValues,
                                  Geolocation geolocation,
                                  String customId,
                                  User user) {
        setName(name);
        this.description = description;
        this.headerImage = headerImage;
        this.putAttributeValues(directAttributeValues, user);
        this.geolocation = geolocation;
        this.customId = customId;
        raiseEvent(new QrBaseSettingUpdatedEvent(this.getId(), appId, user));
        addOpsLog("更新基本设置", user);
    }

    public void updateCustomId(String customId, User user) {
        if (Objects.equals(this.customId, customId)) {
            return;
        }

        this.customId = customId;
        raiseEvent(new QrCustomIdUpdatedEvent(this.getId(), appId, customId, user));
        addOpsLog("自定义编号改为[" + customId + "]", user);
    }

    public void activate(User user) {
        if (this.active) {
            return;
        }

        this.active = true;
        raiseEvent(new QrActivatedEvent(this.getId(), appId, user));
        addOpsLog("启用", user);
    }

    public void deactivate(User user) {
        if (!this.active) {
            return;
        }

        this.active = false;
        raiseEvent(new QrDeactivatedEvent(this.getId(), appId, user));
        addOpsLog("禁用", user);
    }

    public void updateGroupActiveStatus(boolean isActive, User user) {
        this.groupActive = isActive;
        addOpsLog("同步分组状态", user);
    }

    public void access() {
        this.accessCount++;
        this.lastAccessedAt = now();
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

    public void putAttributeValues(Map<String, AttributeValue> values, User user) {
        if (isEmpty(values)) {
            return;
        }

        Map<String, AttributeValue> mergedValues = new HashMap<>();
        mergedValues.putAll(this.attributeValues);
        mergedValues.putAll(values);

        Map<String, AttributeValue> finalValues = mergedValues.entrySet().stream()
                .filter(entry -> isNotBlank(entry.getKey()) && entry.getValue() != null && entry.getValue().isFilled())
                .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!Objects.equals(this.attributeValues, finalValues)) {
            this.attributeValues = finalValues;
            raiseEvent(new QrAttributesUpdatedEvent(this.getId(), appId, user));
        }
    }

    public void setSearchableValues(Set<String> values) {
        this.svs = isNotEmpty(values) ? values : null;
    }

    public Set<String> getSearchableValues() {
        return svs;
    }

    public AttributeValue attributeValueOf(String attributeId) {
        return this.attributeValues.get(attributeId);
    }

    public Optional<AttributeValue> attributeValueOfOptional(String attributeId) {
        return ofNullable(this.attributeValues.get(attributeId));
    }

    public boolean isPositioned() {
        return geolocation != null && geolocation.isPositioned();
    }

    public void cleanAttributeValues(App app, User user) {
        if (this.attributeValues.isEmpty()) {
            return;
        }

        //保证只有存在对应attribute的value才留下
        Set<String> existingAttributeIds = app.allAttributes().stream().map(Attribute::getId).collect(toImmutableSet());
        List<AttributeValue> existingValues = attributeValues.values().stream()
                .filter(value -> existingAttributeIds.contains(value.getAttributeId()))
                .collect(toImmutableList());

        //清洗每个value
        existingValues.forEach(attributeValue -> attributeValue.clean(app));

        //清洗后，只有有值的才留下
        Map<String, AttributeValue> finalValues = existingValues.stream()
                .filter(AttributeValue::isFilled)
                .collect(toImmutableMap(AttributeValue::getAttributeId, identity()));

        if (!Objects.equals(this.attributeValues, finalValues)) {
            this.attributeValues = finalValues;
            raiseEvent(new QrAttributesUpdatedEvent(this.getId(), appId, user));
        }
    }

    public boolean isAttributeValuesEmpty() {
        return this.attributeValues.isEmpty();
    }

    public void updateDescription(String description, User user) {
        if (Objects.equals(this.description, description)) {
            return;
        }

        this.description = description;
        addOpsLog("更新简介", user);
        raiseEvent(new QrDescriptionUpdatedEvent(this.getId(), appId, user));
    }

    public void updateHeaderImage(UploadedFile image, User user) {
        if (Objects.equals(this.headerImage, image)) {
            return;
        }

        this.headerImage = image;
        addOpsLog("更新页眉图片", user);
        raiseEvent(new QrHeaderImageUpdatedEvent(this.getId(), appId, user));
    }

    public void updateGeolocation(Geolocation geolocation, User user) {
        if (Objects.equals(this.geolocation, geolocation)) {
            return;
        }

        this.geolocation = geolocation;
        addOpsLog("更新定位", user);
        raiseEvent(new QrGeolocationUpdatedEvent(this.getId(), appId, user));
    }

    public void checkActive(App app) {
        if (!active) {
            throw new MryException(QR_NOT_ACTIVE, app.instanceDesignation() + "已被禁用！", mapOf("qrId", this.getId()));
        }

        if (!groupActive) {
            throw new MryException(GROUP_NOT_ACTIVE, app.instanceDesignation() + "所在" + app.groupDesignation() + "已被禁用！",
                    mapOf("qrId", this.getId(), "groupId", this.getGroupId()));
        }
    }

    private void setName(String name) {
        this.name = name;
        this.text = ToAnalysis.parse(name).getTerms().stream()
                .map(Term::getRealName)
                .filter(Objects::nonNull)
                .filter(term -> term.matches("[\\u4E00-\\u9FA5]+"))//只保留中文
                .filter(term -> term.length() > 1)
                .limit(3)
                .collect(toImmutableSet());
    }

    public void onDelete(User user) {
        raiseEvent(new QrDeletedEvent(this.getId(), plateId, customId, groupId, appId, user));
    }

    public boolean updateCirculationStatus(String optionId, User user) {
        boolean result = doUpdateCirculationStatus(optionId, user);
        raiseEvent(new QrCirculationStatusChangedEvent(this.getId(), appId, user));
        return result;
    }

    public boolean doUpdateCirculationStatus(String optionId, User user) {
        if (Objects.equals(this.circulationOptionId, optionId)) {
            return false;
        }

        this.circulationOptionId = optionId;
        addOpsLog("流转状态变更为" + optionId, user);
        return true;
    }
}
