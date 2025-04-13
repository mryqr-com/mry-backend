package com.mryqr.core.app.domain;

import com.google.common.collect.Streams;
import com.mryqr.common.domain.AggregateRoot;
import com.mryqr.common.domain.TextOption;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.indexedfield.IndexedFieldRegistry;
import com.mryqr.common.domain.permission.Permission;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.core.app.domain.attribute.*;
import com.mryqr.core.app.domain.event.*;
import com.mryqr.core.app.domain.operationmenu.OperationMenuItem;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.PageInfo;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlInfo;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.TextOptionInfo;
import com.mryqr.core.app.domain.page.setting.SubmitType;
import com.mryqr.core.app.domain.plate.PlateSetting;
import com.mryqr.core.app.domain.report.ReportSetting;
import com.mryqr.core.app.domain.report.chart.ChartReport;
import com.mryqr.core.app.domain.report.number.NumberReport;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.TextAttributeValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.domain.permission.Permission.maxPermission;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.APP_COLLECTION;
import static com.mryqr.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static com.mryqr.core.app.domain.attribute.AttributeType.*;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_INSTANCE;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_MEMBER;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


@Getter
@Document(APP_COLLECTION)
@TypeAlias(APP_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class App extends AggregateRoot {
    private String name;//应用的名称
    private UploadedFile icon;//图标，保存时从setting同步
    private boolean active;//是否启用
    private boolean locked;//是否锁定，锁定之后无法编辑，但是可以正常使用
    private List<String> managers;//应用管理员
    private AppSetting setting;//应用设置
    private String version;//setting所对应的版本，用于保证在多人同时编辑时始终基于最新版本进行更新
    private Permission permission;//App的permission，由所有页面的的最小权限而来
    private Permission operationPermission;//运营所需权限，保存时从AppConfig同步而来
    private IndexedFieldRegistry attributeIndexedValueRegistry;//attributeId -> field，属性值索引字段注册表，保存App时自动构建
    private Map<String, IndexedFieldRegistry> controlIndexedValueRegistries;//pageId -> registry(controlId -> field)，控件答案索引字段注册表，保存App时自动构建
    private boolean hasWeeklyResetAttributes;//是否存在有每周初需要重新计算的自定义属性
    private boolean hasMonthlyResetAttributes;//是否存在有每月初需要重新计算的自定义属性
    private boolean hasSeasonlyResetAttributes;//是否存在有每季度初需要重新计算的自定义属性
    private boolean hasYearlyResetAttributes;//是否存在有每年初需要重新计算的自定义属性
    private ReportSetting reportSetting;//报告设置
    private WebhookSetting webhookSetting;//Webhook配置
    private String sourceAppId;//从哪个App拷贝来的
    private String appTemplateId;//从哪个应用模板而来的
    private boolean groupSynced;//分组是否从部门同步

    public App(String name, AppSetting setting, User user) {
        super(newAppId(), user);
        init(name, setting, ReportSetting.create(), user);
        addOpsLog("新建", user);
    }

    public App(String name, App sourceApp, User user) {
        super(newAppId(), sourceApp.getTenantId(), user);
        init(name, sourceApp.getSetting(), sourceApp.getReportSetting(), user);
        sourceAppId = sourceApp.getId();
        addOpsLog("复制新建", user);
    }

    public App(QR appTemplate, App templateApp, User user) {
        super(newAppId(), user);
        init(appTemplate.getName(), templateApp.getSetting(), templateApp.getReportSetting(), user);
        this.appTemplateId = appTemplate.getId();
        this.sourceAppId = templateApp.getId();
        raiseEvent(new AppCreatedFromTemplateEvent(this.getAppTemplateId(), this.sourceAppId, this.getId(), user));
        addOpsLog("从应用模板新建", user);
    }

    public App(String id, String name, AppSetting setting, User user) {
        super(id, user);
        init(name, setting, ReportSetting.create(), user);
        addOpsLog("新建", user);
    }

    private void init(String name, AppSetting setting, ReportSetting reportSetting, User user) {
        this.version = increaseVersion();
        this.name = name;
        this.active = true;
        this.locked = false;
        this.managers = List.of();
        this.attributeIndexedValueRegistry = IndexedFieldRegistry.create();
        this.controlIndexedValueRegistries = new HashMap<>();
        correctAndValidate(setting.context());
        doUpdateSetting(setting);
        this.reportSetting = reportSetting;
        this.webhookSetting = WebhookSetting.create();
        raiseEvent(new AppCreatedEvent(this.getId(), user));
    }

    public static String newAppId() {
        return "APP" + newSnowflakeId();
    }

    public UpdateAppSettingResult updateSetting(AppSetting newSetting, String version, User user) {
        if (!Objects.equals(this.getVersion(), version)) {
            throw new MryException(APP_ALREADY_UPDATED, "更新失败，应用已经在别处被更新，请刷新页面后重新编辑。", mapOf("appId", this.getId()));
        }

        this.version = increaseVersion();

        if (this.locked) {
            throw new MryException(APP_ALREADY_LOCKED, "应用已经锁定，无法编辑，请解除锁定后再编辑。", mapOf("appId", this.getId()));
        }

        AppSettingContext newContext = newSetting.context();
        correctAndValidate(newContext);
        AppSettingContext oldContext = this.setting.context();
        oldContext.validateControlTypesNotModified(newContext);
        oldContext.validateAttributesSchemaNotModified(newContext);

        checkPagesAndControlsDeletion(oldContext, newContext, user);
        checkAttributeAdditionAndDeletion(oldContext, newContext, user);
        checkPageSubmitTypeChanges(oldContext, newContext, user);

        doUpdateSetting(newSetting);

        Set<ControlType> newlyAddedControlTypes = oldContext.calculateNewlyAddedControlTypes(newContext);
        this.addOpsLog("编辑", user);
        return UpdateAppSettingResult.builder().newlyAddedControlTypes(newlyAddedControlTypes).build();
    }

    private void correctAndValidate(AppSettingContext context) {
        try {
            context.correctAndValidate();
        } catch (MryException ex) {
            ex.addData("appId", this.getId());
            throw ex;
        }
    }

    private void doUpdateSetting(AppSetting setting) {
        this.setting = setting;
        this.icon = this.setting.getConfig().getIcon();
        this.permission = this.setting.minPagePermission();//影响单个QR的查看和提交权限，采用最小有限原则
        this.operationPermission = maxPermission(this.setting.getConfig().getOperationPermission(), permission);//运营端权限，采用最大优先原则
        this.allAttributes().stream().map(Attribute::getId).forEach(attributeIndexedValueRegistry::registerKey);
        this.allPages().forEach(page -> page.getControls().stream().map(Control::getId)
                .forEach(getOrCreateIndexedValueRegistryForPage(page.getId())::registerKey));
        hasWeeklyResetAttributes = allAttributes().stream().anyMatch(Attribute::shouldWeeklyReset);
        hasMonthlyResetAttributes = allAttributes().stream().anyMatch(Attribute::shouldMonthlyReset);
        hasSeasonlyResetAttributes = allAttributes().stream().anyMatch(Attribute::shouldSeasonlyReset);
        hasYearlyResetAttributes = allAttributes().stream().anyMatch(Attribute::shouldYearlyReset);
    }

    private String increaseVersion() {
        if (isBlank(this.version)) {
            return String.valueOf(1);
        }

        return String.valueOf(Integer.parseInt(this.version) + 1);
    }

    private IndexedFieldRegistry getOrCreateIndexedValueRegistryForPage(String pageId) {
        IndexedFieldRegistry registry = controlIndexedValueRegistries.get(pageId);

        if (registry == null) {
            registry = IndexedFieldRegistry.create();
            controlIndexedValueRegistries.put(pageId, registry);
        }

        return registry;
    }

    private void checkPagesAndControlsDeletion(AppSettingContext oldContext, AppSettingContext newContext, User user) {
        Set<PageInfo> deletedPages = oldContext.calculateDeletedPages(newContext);
        Set<String> deletedPageIds = deletedPages.stream().map(PageInfo::getPageId).collect(toImmutableSet());

        if (isNotEmpty(deletedPages)) {
            deletedPages.forEach(deletedPage -> controlIndexedValueRegistries.remove(deletedPage.getPageId()));
            raiseEvent(new AppPagesDeletedEvent(this.getId(), deletedPages, user));
            this.reportSetting.removePageAwareReports(deletedPageIds);
        }

        Set<ControlInfo> deletedControls = oldContext.calculateDeletedControls(newContext, deletedPageIds);//删除的控件没有必要包含被删除页面所包含的控件
        Set<DeletedControlInfo> deletedControlInfos = deletedControls.stream().map(controlInfo -> {
            IndexedFieldRegistry indexedFieldRegistry = getOrCreateIndexedValueRegistryForPage(controlInfo.getPageId());
            IndexedField indexedField = indexedFieldRegistry.fieldByKey(controlInfo.getControlId());
            indexedFieldRegistry.removeKey(controlInfo.getControlId());

            return DeletedControlInfo.builder()
                    .pageId(controlInfo.getPageId())
                    .controlId(controlInfo.getControlId())
                    .indexedField(indexedField)
                    .controlType(controlInfo.getControlType())
                    .build();
        }).collect(toImmutableSet());

        if (isNotEmpty(deletedControlInfos)) {
            raiseEvent(new AppControlsDeletedEvent(this.getId(), deletedControlInfos, user));
            Set<String> deletedControlIds = deletedControlInfos.stream().map(DeletedControlInfo::getControlId)
                    .collect(toImmutableSet());
            this.reportSetting.removeControlAwareReports(deletedControlIds);
        }

        Set<String> allDeletedControlIds = Streams.concat(deletedPages.stream().map(PageInfo::getControlInfos)
                        .flatMap(Collection::stream), deletedControls.stream())
                .map(ControlInfo::getControlId)
                .collect(toImmutableSet());

        //单单option被删除的情况，需要排除所有被删除的control
        Set<TextOptionInfo> deletedTextOptions = oldContext.calculateDeletedTextOptions(newContext, allDeletedControlIds);
        Set<DeletedTextOptionInfo> deletedTextOptionInfos = deletedTextOptions.stream()
                .map(info -> {
                    String controlId = info.getControlId();
                    String pageId = oldContext.pageForControl(controlId).getId();
                    return DeletedTextOptionInfo.builder()
                            .controlId(controlId)
                            .pageId(pageId)
                            .controlType(info.getControlType())
                            .optionId(info.getOptionId())
                            .build();
                }).collect(toImmutableSet());

        if (isNotEmpty(deletedTextOptionInfos)) {
            raiseEvent(new AppControlOptionsDeletedEvent(this.getId(), deletedTextOptionInfos, user));
        }
    }

    private void checkAttributeAdditionAndDeletion(AppSettingContext oldContext, AppSettingContext newContext, User user) {
        AttributeCheckChangeResult result = oldContext.calculateAttributeChanges(newContext);
        Set<AttributeInfo> deletedAttributes = result.getDeletedAttributes();

        Set<DeletedAttributeInfo> deletedAttributeInfos = deletedAttributes.stream().map(attribute -> {
            IndexedField indexedField = attributeIndexedValueRegistry.fieldByKey(attribute.getAttributeId());
            attributeIndexedValueRegistry.removeKey(attribute.getAttributeId());

            return DeletedAttributeInfo.builder()
                    .attributeId(attribute.getAttributeId())
                    .type(attribute.getAttributeType())
                    .valueType(attribute.getValueType())
                    .indexedField(indexedField)
                    .build();

        }).collect(toImmutableSet());

        if (isNotEmpty(deletedAttributeInfos)) {
            raiseEvent(new AppAttributesDeletedEvent(this.getId(), deletedAttributeInfos, user));
            Set<String> deletedAttributeIds = deletedAttributeInfos.stream().map(DeletedAttributeInfo::getAttributeId)
                    .collect(toImmutableSet());
            this.reportSetting.removeAttributeAwareReports(deletedAttributeIds);
        }

        Set<AttributeInfo> createdAttributes = result.getCreatedAttributes();
        if (isNotEmpty(createdAttributes)) {
            raiseEvent(new AppAttributesCreatedEvent(this.getId(), createdAttributes, user));
        }
    }

    private void checkPageSubmitTypeChanges(AppSettingContext oldContext, AppSettingContext newContext, User user) {
        Set<String> changedToPerInstancePageIds = oldContext.calculateSubmitTypeChanges(newContext, ONCE_PER_INSTANCE);
        if (isNotEmpty(changedToPerInstancePageIds)) {
            raiseEvent(new AppPageChangedToSubmitPerInstanceEvent(this.getId(), changedToPerInstancePageIds, this.getVersion(), user));
        }

        Set<String> changedToPerMemberPageIds = oldContext.calculateSubmitTypeChanges(newContext, ONCE_PER_MEMBER);
        if (isNotEmpty(changedToPerMemberPageIds)) {
            raiseEvent(new AppPageChangedToSubmitPerMemberEvent(this.getId(), changedToPerMemberPageIds, this.getVersion(), user));
        }
    }

    public void updateReportSetting(ReportSetting reportSetting, User user) {
        AppSettingContext context = this.setting.context();
        try {
            reportSetting.correct();
            reportSetting.validate(context);
        } catch (MryException ex) {
            ex.addData("appId", this.getId());
            throw ex;
        }

        this.reportSetting = reportSetting;
        addOpsLog("编辑报表", user);
    }

    public void updateWebhookSetting(WebhookSetting setting, User user) {
        this.webhookSetting = setting;
        addOpsLog("更新Webhook配置", user);
    }

    public void activate(User user) {
        if (this.active) {
            return;
        }

        this.active = true;
        addOpsLog("启用", user);
    }

    public void deactivate(User user) {
        if (!this.active) {
            return;
        }

        this.active = false;
        addOpsLog("禁用", user);
    }

    public void lock(User user) {
        if (this.locked) {
            return;
        }

        this.locked = true;
        addOpsLog("锁定", user);
    }

    public void unlock(User user) {
        if (!this.locked) {
            return;
        }

        this.locked = false;
        addOpsLog("解除锁定", user);
    }

    public void rename(String name, User user) {
        if (Objects.equals(this.name, name)) {
            return;
        }

        this.name = name;
        addOpsLog("重命名为[" + name + "]", user);
    }

    public void setManagers(List<String> managerIds, User user) {
        if (Objects.equals(this.managers, managerIds)) {
            return;
        }

        this.managers = managerIds;
        addOpsLog("更新管理员", user);
    }

    public void deactivateWebhook(User user) {
        this.webhookSetting.deactivate();
        addOpsLog("停用Webhook", user);
    }

    public void enableGroupSync(User user) {
        if (this.groupSynced) {
            return;
        }

        this.groupSynced = true;
        addOpsLog("启用分组与部门同步", user);
        raiseEvent(new AppGroupSyncEnabledEvent(this.getId(), user));
    }

    public String homePageId() {
        return setting.homePageId();
    }

    public boolean requireLogin() {
        return permission.requireLogin();
    }

    public boolean isPublic() {
        return permission.isPublic();
    }

    public Permission requiredPermission() {
        return permission;
    }

    public boolean containsManager(String memberId) {
        return managers.contains(memberId);
    }

    public boolean notAllowDuplicateInstanceName() {
        return !this.getSetting().getConfig().isAllowDuplicateInstanceName();
    }

    public void checkActive() {
        if (!active) {
            throw new MryException(APP_NOT_ACTIVE, "应用已被禁用！", mapOf("appId", this.getId()));
        }
    }

    public List<Control> allControls() {
        return setting.getPages().stream().map(Page::getControls).flatMap(Collection::stream)
                .collect(toImmutableList());
    }

    public List<Page> allPages() {
        return List.copyOf(setting.getPages());
    }

    public List<Page> allFillablePages() {
        return setting.getPages().stream().filter(Page::isFillable).collect(toImmutableList());
    }

    public List<Page> allApprovablePages() {
        return setting.getPages().stream().filter(Page::isApprovalEnabled).collect(toImmutableList());
    }

    public Page pageById(String pageId) {
        try {
            return setting.pageById(pageId);
        } catch (MryException e) {
            e.addData("appId", this.getId());
            throw e;
        }
    }

    public Optional<Page> pageByIdOptional(String pageId) {
        return setting.pageByIdOptional(pageId);
    }

    public Control controlById(String controlId) {
        try {
            return setting.controlById(controlId);
        } catch (MryException e) {
            e.addData("appId", this.getId());
            throw e;
        }
    }

    public Optional<Control> controlByIdOptional(String controlId) {
        return setting.controlByIdOptional(controlId);
    }

    public List<Attribute> allAttributes() {
        return List.copyOf(setting.getAttributes());
    }

    public Map<String, AttributeValue> fixedAttributeValues() {
        return this.allAttributes().stream()
                .filter(Attribute::isFixed)
                .map(attribute -> new TextAttributeValue(attribute, attribute.getFixedValue()))
                .collect(toImmutableMap(AttributeValue::getAttributeId, identity()));
    }

    public Map<String, AttributeValue> allFixedAttributeValues() {
        return this.allAttributes().stream()
                .filter(Attribute::isFixed)
                .map(attribute -> new TextAttributeValue(attribute, attribute.getFixedValue()))
                .collect(toImmutableMap(AttributeValue::getAttributeId, identity()));
    }

    public List<Attribute> allCalculatedAttributes() {
        return allAttributes().stream()
                .filter(Attribute::isCalculated)
                .collect(toImmutableList());
    }

    public List<Attribute> allAttributesOfIds(Collection<String> attributeIds) {
        return allAttributes().stream().filter(attribute -> attributeIds.contains(attribute.getId()))
                .collect(toImmutableList());
    }

    public List<Attribute> allAttributesOfTypes(AttributeType... types) {
        return allAttributesOfTypes(asList(types));
    }

    public List<Attribute> allAttributesOfTypes(Collection<AttributeType> types) {
        return allAttributes().stream()
                .filter(attribute -> types.contains(attribute.getType()))
                .collect(toImmutableList());
    }

    public List<Attribute> allAttributesOfRange(AttributeStatisticRange range) {
        return allAttributes().stream()
                .filter(attribute -> attribute.getRange() == range)
                .collect(toImmutableList());
    }

    public List<Attribute> allSubmissionAwareAttributes() {
        return allAttributes().stream()
                .filter(Attribute::isSubmissionAware)
                .collect(toImmutableList());
    }

    public List<Attribute> allPageSubmissionAwareAttributes(String pageId) {
        return allAttributes()
                .stream()
                .filter(it -> it.getType() == INSTANCE_SUBMIT_COUNT ||//针对页面的提交增减也将影响整个instance的提交计数
                              it.getType() == INSTANCE_CIRCULATION_STATUS && this.circulationStatusAfterSubmission(pageId).isPresent() ||
                              it.isSubmissionAware() && Objects.equals(pageId, it.getPageId()))
                .collect(toImmutableList());
    }

    public List<Attribute> controlReferencedAttributes(String controlId) {
        return allAttributes().stream().filter(attribute -> controlId.equals(attribute.getControlId()))
                .collect(toImmutableList());
    }

    public boolean hasAttribute(String attributeId) {
        return allAttributes().stream().anyMatch(attribute -> attribute.getId().equals(attributeId));
    }

    public Optional<IndexedField> indexedFieldForControlOptional(String pageId, String controlId) {
        return indexedValueRegistryForPageOptional(pageId)
                .flatMap(registry -> registry.fieldByKeyOptional(controlId));
    }

    public IndexedField indexedFieldForControl(String pageId, String controlId) {
        return indexedValueRegistryForPage(pageId).fieldByKey(controlId);
    }

    public boolean hasControlIndexField(String pageId, IndexedField indexedField) {
        return indexedValueRegistryForPageOptional(pageId).map(registry -> registry.hasField(indexedField)).orElse(false);
    }

    public boolean hasControlIndexKey(String pageId, String key) {
        return indexedValueRegistryForPageOptional(pageId).map(registry -> registry.hasKey(key)).orElse(false);
    }

    public Set<String> summaryEligibleAttributeIds() {
        return allAttributes().stream()
                .filter(Attribute::isPcListEligible)
                .map(Attribute::getId)
                .collect(toImmutableSet());
    }

    private Optional<IndexedFieldRegistry> indexedValueRegistryForPageOptional(String pageId) {
        return ofNullable(controlIndexedValueRegistries.get(pageId));
    }

    private IndexedFieldRegistry indexedValueRegistryForPage(String pageId) {
        IndexedFieldRegistry registry = controlIndexedValueRegistries.get(pageId);
        if (registry == null) {
            throw new RuntimeException("Indexed value registry not found for page[" + pageId + "].");
        }

        return registry;
    }

    public Optional<IndexedField> indexedFieldForAttributeOptional(String attributeId) {
        return attributeIndexedValueRegistry.fieldByKeyOptional(attributeId);
    }

    public IndexedField indexedFieldForAttribute(String attributeId) {
        return attributeIndexedValueRegistry.fieldByKey(attributeId);
    }

    public boolean hasAttributeIndexedFiled(IndexedField indexedField) {
        return attributeIndexedValueRegistry.hasField(indexedField);
    }

    public boolean hasAttributeIndexKey(String key) {
        return attributeIndexedValueRegistry.hasKey(key);
    }

    public String instanceDesignation() {
        String instanceAlias = setting.getConfig().getInstanceAlias();
        return isNotBlank(instanceAlias) ? instanceAlias : "实例";
    }

    public String groupDesignation() {
        String groupAlias = setting.getConfig().getGroupAlias();
        return isNotBlank(groupAlias) ? groupAlias : "分组";
    }

    public String customIdDesignation() {
        String customIdAlias = setting.getConfig().getCustomIdAlias();
        return isNotBlank(customIdAlias) ? customIdAlias : "自定义编号";
    }

    public String qrImportNameFieldName() {
        return "名称（必填）";
    }

    public String qrImportCustomIdFieldName() {
        return this.customIdDesignation() + "（必填）";
    }

    public List<Attribute> allExportableAttributes() {
        return setting.getAttributes().stream().filter(Attribute::isValueExportable)
                .collect(toImmutableList());
    }

    public void modifyPagesSetting(Map<String, Pair<SubmitType, Permission>> modifiers) {
        this.getSetting().getPages().forEach(page -> {
            Pair<SubmitType, Permission> pair = modifiers.get(page.getId());
            if (pair != null) {
                page.modify(pair.getLeft(), pair.getRight());
            }
        });
    }

    public List<QrWebhookType> qrWebhookTypes() {
        return List.copyOf(this.setting.getConfig().getQrWebhookTypes());
    }

    public Optional<Attribute> attributeByIdOptional(String attributeId) {
        return setting.getAttributes().stream().filter(attribute -> attribute.getId().equals(attributeId)).findFirst();
    }

    public Attribute attributeById(String attributeId) {
        return attributeByIdOptional(attributeId)
                .orElseThrow(() -> new MryException(ATTRIBUTE_NOT_FOUND, "属性项不存在。",
                        mapOf("attributeId", attributeId)));
    }

    public PlateSetting plateSetting() {
        return this.getSetting().getPlateSetting();
    }

    public List<Attribute> directAttributes() {
        return this.allAttributes().stream()
                .filter(attribute -> attribute.getType() == DIRECT_INPUT)
                .collect(toImmutableList());
    }

    public List<Attribute> manualInputAttributes() {
        return this.directAttributes().stream()
                .filter(Attribute::isManualInput)
                .collect(toImmutableList());
    }

    public List<Attribute> qrImportableAttributes() {
        return this.allAttributes().stream().filter(attribute -> {
            if (attribute.getType() == DIRECT_INPUT) {
                return true;
            }

            if (attribute.isPageAware() &&
                attribute.isControlAware() &&
                (attribute.getType() == CONTROL_LAST || attribute.getType() == CONTROL_FIRST)) {
                Page page = pageById(attribute.getPageId());
                if (page.isOncePerInstanceSubmitType()) {
                    Control control = controlById(attribute.getControlId());
                    return control.getType().isQrImportable();
                }
                return false;
            }
            return false;
        }).collect(toImmutableList());
    }

    public boolean isWebhookEnabled() {
        return this.webhookSetting.isEnabled() && !this.webhookSetting.isNotAccessible();
    }

    public void onDelete(User user) {
        this.raiseEvent(new AppDeletedEvent(this.getId(), user));
    }

    public int controlCount() {
        return this.allControls().size();
    }

    public boolean isGeolocationEnabled() {
        return this.setting.getConfig().isGeolocationEnabled();
    }

    public boolean isPlateBatchEnabled() {
        return this.setting.getConfig().isPlateBatchEnabled();
    }

    public boolean isAssignmentEnabled() {
        return this.setting.getConfig().isAssignmentEnabled();
    }

    public List<String> numberReportNames() {
        return this.reportSetting.getNumberReportSetting().getReports().stream().map(NumberReport::getName).collect(toImmutableList());
    }

    public List<String> chartReportNames() {
        return this.reportSetting.getChartReportSetting().getReports().stream().map(ChartReport::getName).collect(toImmutableList());
    }

    public List<String> kanbanNames() {
        return this.allAttributes().stream().filter(Attribute::isKanbanEligible).map(Attribute::getName).collect(toImmutableList());
    }

    public List<TextOption> circulationOptions() {
        return this.setting.getCirculationStatusSetting().getOptions();
    }

    public List<String> circulationStatusNames() {
        return this.setting.getCirculationStatusSetting().getOptions().stream().map(TextOption::getName).collect(toImmutableList());
    }

    public List<String> pageNames() {
        return this.allPages().stream().map(Page::pageName).collect(toImmutableList());
    }

    public List<String> fillablePageNames() {
        return this.allPages().stream().filter(Page::isFillable).map(Page::pageName).collect(toImmutableList());
    }

    public List<String> approvalPageNames() {
        return this.allPages().stream().filter(Page::isApprovalEnabled).map(Page::pageName).collect(toImmutableList());
    }

    public List<String> notificationPageNames() {
        return this.allPages().stream().filter(Page::isNotificationEnabled).map(Page::pageName).collect(toImmutableList());
    }

    public List<String> attributeNames() {
        return this.allAttributes().stream().map(Attribute::getName).collect(toImmutableList());
    }

    public List<String> operationMenuNames() {
        return this.setting.getOperationMenuItems().stream().map(OperationMenuItem::getName).collect(toImmutableList());
    }

    public String circulationInitOptionId() {
        return this.setting.getCirculationStatusSetting().getInitOptionId();
    }

    public Optional<String> circulationStatusAfterSubmission(String pageId) {
        return this.setting.getCirculationStatusSetting().statusAfterSubmission(pageId);
    }

    public boolean canSubmitByCirculationStatus(String circulationStatusOptionId, String pageId) {
        return this.setting.getCirculationStatusSetting().canSubmit(circulationStatusOptionId, pageId);
    }

    public boolean hasCirculationStatus(String circulationStatusOptionId) {
        return this.setting.getCirculationStatusSetting().getOptions().stream()
                .anyMatch(option -> Objects.equals(option.getId(), circulationStatusOptionId));
    }
}
