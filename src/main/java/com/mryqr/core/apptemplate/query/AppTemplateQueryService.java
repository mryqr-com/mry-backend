package com.mryqr.core.apptemplate.query;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.Pagination;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.plan.domain.PlanType;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.attribute.*;
import com.mryqr.core.qr.query.plate.QrPlateAttributeValueQueryService;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.common.exception.ErrorCode.APP_TEMPLATE_NOT_PUBLISHED;
import static com.mryqr.common.exception.ErrorCode.QR_NOT_FOUND;
import static com.mryqr.common.utils.CommonUtils.splitSearchBySpace;
import static com.mryqr.common.utils.MongoCriteriaUtils.mongoSortableFieldOf;
import static com.mryqr.common.utils.MongoCriteriaUtils.mongoTextFieldOf;
import static com.mryqr.common.utils.MryConstants.QR_COLLECTION;
import static com.mryqr.common.utils.Pagination.pagination;
import static com.mryqr.management.apptemplate.MryAppTemplateManageApp.*;
import static com.mryqr.management.common.PlanTypeControl.OPTION_TO_PLAN_MAP;
import static java.util.List.of;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class AppTemplateQueryService {
    private final MryRateLimiter mryRateLimiter;
    private final AppRepository appRepository;
    private final MongoTemplate mongoTemplate;
    private final QrRepository qrRepository;
    private final GroupRepository groupRepository;
    private final TenantRepository tenantRepository;
    private final QrPlateAttributeValueQueryService qrPlateAttributeValueQueryService;

    public PagedList<QListAppTemplate> listPublishedAppTemplates(ListAppTemplateQuery queryCommand) {
        mryRateLimiter.applyFor("AppTemplate:List", 50);

        App app = appRepository.cachedById(MRY_APP_TEMPLATE_MANAGE_APP_ID);

        Criteria criteria = where("appId").is(MRY_APP_TEMPLATE_MANAGE_APP_ID)
                .and("customId").ne(null).and("headerImage").ne(null);//保证仅返回设置完整的模板

        app.indexedFieldForAttributeOptional(STATUS_ATTRIBUTE_ID)
                .ifPresent(indexedField -> criteria.and(mongoTextFieldOf(indexedField)).is(PUBLISHED_STATUS_OPTION_ID));

        String category = queryCommand.getCategory();
        if (isNotBlank(category)) {
            app.indexedFieldForAttributeOptional(CATEGORIES_ATTRIBUTE_ID)
                    .ifPresent(indexedField -> criteria.and(mongoTextFieldOf(indexedField)).is(category));
        }

        String scenario = queryCommand.getScenario();
        if (isNotBlank(scenario)) {
            app.indexedFieldForAttributeOptional(SCENARIO_ATTRIBUTE_ID)
                    .ifPresent(indexedField -> criteria.and(mongoTextFieldOf(indexedField)).is(scenario));
        }

        String search = queryCommand.getSearch();
        if (isNotBlank(search)) {
            Object[] terms = splitSearchBySpace(search);
            criteria.orOperator(where("svs").all(terms), where("text").all(terms));
        }

        Query query = query(criteria);
        Pagination pagination = pagination(queryCommand.getPageIndex(), queryCommand.getPageSize());
        long count = mongoTemplate.count(query, QR_COLLECTION);
        if (count == 0) {
            return pagedList(pagination, 0, of());
        }

        Sort sort = app.indexedFieldForAttributeOptional(DISPLAY_ORDER_ATTRIBUTE_ID)
                .map(indexedField -> by(DESC, mongoSortableFieldOf(indexedField)).and(by(DESC, "createdAt")))
                .orElse(by(DESC, "createdAt"));

        query.skip(pagination.skip()).limit(pagination.limit()).with(sort);
        query.fields().include("name", "headerImage", "attributeValues");
        List<QRawQr> qrs = mongoTemplate.find(query, QRawQr.class, QR_COLLECTION);

        List<QListAppTemplate> templates = qrs.stream().map(qRawQr -> {
            AttributeValue planTypeValue = qRawQr.attributeValues.get(TEMPLATE_PLAN_TYPE_ATTRIBUTE_ID);
            PlanType planType = planTypeValue != null ?
                    OPTION_TO_PLAN_MAP.get(((DropdownAttributeValue) planTypeValue).getOptionIds().get(0)) : null;

            AttributeValue featureValue = qRawQr.attributeValues.get(FEATURE_ATTRIBUTE_ID);
            List<String> features = featureValue != null ?
                    ((DropdownAttributeValue) featureValue).getOptionIds().stream().map(FEATURE_NAMES_MAP::get).collect(toImmutableList()) : of();

            AttributeValue cardDescriptionValue = qRawQr.attributeValues.get(CARD_DESCRIPTION_ATTRIBUTE_ID);
            String cardDescription = cardDescriptionValue != null ?
                    ((TextAttributeValue) cardDescriptionValue).getText() : null;

            return QListAppTemplate.builder()
                    .id(qRawQr.getId())
                    .name(qRawQr.getName())
                    .poster(qRawQr.getHeaderImage())
                    .planType(planType)
                    .features(features)
                    .cardDescription(cardDescription)
                    .build();
        }).collect(toImmutableList());

        return pagedList(pagination, (int) count, templates);
    }

    private PagedList<QListAppTemplate> pagedList(Pagination pagination, int count, List<QListAppTemplate> objects) {
        return PagedList.<QListAppTemplate>builder()
                .totalNumber(count)
                .pageSize(pagination.getPageSize())
                .pageIndex(pagination.getPageIndex())
                .data(objects)
                .build();
    }

    public QDetailedAppTemplate fetchAppTemplateDetail(String appTemplateId) {
        mryRateLimiter.applyFor("AppTemplate:FetchDetail", 50);

        QR appTemplate = qrRepository.byId(appTemplateId);

        AttributeValue attributeValue = appTemplate.attributeValueOf(STATUS_ATTRIBUTE_ID);
        if (attributeValue == null) {
            throw new MryException(APP_TEMPLATE_NOT_PUBLISHED, "模板尚未发布");
        }

        ItemStatusAttributeValue statusAttributeValue = (ItemStatusAttributeValue) attributeValue;
        if (!Objects.equals(statusAttributeValue.getOptionId(), PUBLISHED_STATUS_OPTION_ID)) {
            throw new MryException(APP_TEMPLATE_NOT_PUBLISHED, "模板尚未发布");
        }

        AttributeValue planTypeValue = appTemplate.attributeValueOf(TEMPLATE_PLAN_TYPE_ATTRIBUTE_ID);
        PlanType planType = planTypeValue != null ?
                OPTION_TO_PLAN_MAP.get(((DropdownAttributeValue) planTypeValue).getOptionIds().get(0)) : null;

        AttributeValue cardDescriptionValue = appTemplate.attributeValueOf(CARD_DESCRIPTION_ATTRIBUTE_ID);
        String cardDescription = cardDescriptionValue != null ?
                ((TextAttributeValue) cardDescriptionValue).getText() : null;

        AttributeValue introductionValue = appTemplate.attributeValueOf(INTRODUCTION_ATTRIBUTE_ID);
        String introduction = introductionValue != null ?
                ((MultiLineTextAttributeValue) introductionValue).getContent() : null;

        AttributeValue demoQrValue = appTemplate.attributeValueOf(DEMO_QR_ATTRIBUTE_ID);
        String demoQrPlateId = ((IdentifierAttributeValue) demoQrValue).getContent();

        String templateAppId = appTemplate.getCustomId();
        App templateApp = appRepository.cachedById(templateAppId);

        Optional<QR> qr = qrRepository.byPlateIdOptional(demoQrPlateId);
        if (qr.isEmpty()) {
            throw new MryException(QR_NOT_FOUND, "未找到演示实例。");
        }

        QR demoQr = qr.get();
        Group group = groupRepository.cachedById(demoQr.getGroupId());
        Map<String, String> attributeValues = qrPlateAttributeValueQueryService.fetchQrPlateAttributeValues(templateApp, demoQr);
        Tenant tenant = tenantRepository.cachedById(group.getTenantId());
        QAppTemplateDemoQr qDemoQr = QAppTemplateDemoQr.builder()
                .id(demoQr.getId())
                .name(demoQr.getName())
                .headerImage(demoQr.getHeaderImage())
                .customId(demoQr.getCustomId())
                .plateId(demoQr.getPlateId())
                .attributeValues(attributeValues)
                .appName(templateApp.getName())
                .tenantName(tenant.getName())
                .groupId(demoQr.getGroupId())
                .groupName(group.getName())
                .build();

        return QDetailedAppTemplate.builder()
                .id(appTemplate.getId())
                .name(appTemplate.getName())
                .planType(planType)
                .cardDescription(cardDescription)
                .introduction(introduction)
                .plateSetting(templateApp.plateSetting())
                .demoQr(qDemoQr)
                .controlCount(templateApp.controlCount())
                .geolocationEnabled(templateApp.isGeolocationEnabled())
                .plateBatchEnabled(templateApp.isPlateBatchEnabled())
                .assignmentEnabled(templateApp.isAssignmentEnabled())
                .numberReports(templateApp.numberReportNames())
                .chartReports(templateApp.chartReportNames())
                .kanbans(templateApp.kanbanNames())
                .circulationStatuses(templateApp.circulationStatusNames())
                .pages(templateApp.pageNames())
                .fillablePages(templateApp.fillablePageNames())
                .approvalPages(templateApp.approvalPageNames())
                .notificationPages(templateApp.notificationPageNames())
                .attributes(templateApp.attributeNames())
                .operationMenus(templateApp.operationMenuNames())
                .build();
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    static class QRawQr {
        private String id;
        private String name;
        private UploadedFile headerImage;
        private Map<String, AttributeValue> attributeValues;
    }
}
