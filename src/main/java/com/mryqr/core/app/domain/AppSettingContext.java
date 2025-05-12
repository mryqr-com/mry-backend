package com.mryqr.core.app.domain;

import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeCheckChangeResult;
import com.mryqr.core.app.domain.attribute.AttributeInfo;
import com.mryqr.core.app.domain.operationmenu.OperationMenuItem;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.PageInfo;
import com.mryqr.core.app.domain.page.control.AbstractTextOptionControl;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlInfo;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.TextOptionInfo;
import com.mryqr.core.app.domain.page.setting.SubmitType;
import com.mryqr.core.common.exception.MryException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.exception.ErrorCode.ATTRIBUTE_ID_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.ATTRIBUTE_NAME_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.ATTRIBUTE_NOT_FOUND;
import static com.mryqr.core.common.exception.ErrorCode.ATTRIBUTE_SCHEMA_CANNOT_MODIFIED;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_ID_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_NOT_FOUND;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_TYPE_NOT_MATCH;
import static com.mryqr.core.common.exception.ErrorCode.NO_APP_HOME_PAGE;
import static com.mryqr.core.common.exception.ErrorCode.OPERATION_MENU_ITEM_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.OPERATION_MENU_ITEM_ID_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.OPERATION_MENU_ITEM_NAME_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.PAGE_ID_DUPLICATED;
import static com.mryqr.core.common.utils.Identified.isDuplicated;
import static com.mryqr.core.common.utils.MapUtils.mapOf;

public class AppSettingContext {
    private final AppSetting appSetting;

    private final Set<String> allPageIds;
    private final Map<String, Page> allPages;

    private final Map<String, Control> allControls;
    private final Map<String, ControlInfo> allControlInfos;

    private final Set<String> allAttributeIds;
    private final Map<String, Attribute> allAttributes;

    public AppSettingContext(AppSetting appSetting) {
        this.appSetting = appSetting;

        Set<String> allPageIds = new HashSet<>();
        Map<String, Page> allPages = new HashMap<>();
        Map<String, Control> allControls = new HashMap<>();
        Map<String, ControlInfo> allControlInfos = new HashMap<>();
        Set<String> allAttributeIds = new HashSet<>();
        Map<String, Attribute> allAttributes = new HashMap<>();

        appSetting.getPages().forEach(page -> {
            allPageIds.add(page.getId());
            allPages.put(page.getId(), page);
            page.getControls().forEach(control -> {
                allControls.put(control.getId(), control);
                ControlInfo controlInfo = ControlInfo.builder()
                        .pageId(page.getId())
                        .controlId(control.getId())
                        .controlType(control.getType())
                        .build();
                allControlInfos.put(control.getId(), controlInfo);
            });
        });

        appSetting.getAttributes().forEach(attribute -> {
            allAttributeIds.add(attribute.getId());
            allAttributes.put(attribute.getId(), attribute);
        });

        this.allPageIds = Set.copyOf(allPageIds);
        this.allPages = Map.copyOf(allPages);
        this.allControls = Map.copyOf(allControls);
        this.allControlInfos = Map.copyOf(allControlInfos);
        this.allAttributeIds = Set.copyOf(allAttributeIds);
        this.allAttributes = Map.copyOf(allAttributes);
    }

    public void correctAndValidate() {
        validateHomePageExists();

        appSetting.getPages().forEach(page -> page.correct(this));
        appSetting.getMenu().correct();
        appSetting.getPlateSetting().correct();
        appSetting.getCirculationStatusSetting().correct();
        appSetting.getAttributes().forEach(Attribute::correct);

        validatePageIdsNoDuplication();
        validateAttributeIdsNoDuplication();
        validateAttributeNamesNoDuplication();
        validateAllControlIdsNoDuplication();
        validateOperationMenuItemsNoDuplication();

        appSetting.getConfig().validate();
        appSetting.getPages().forEach(page -> page.validate(this));
        appSetting.getMenu().validate(this);
        appSetting.getPlateSetting().validate(this);
        appSetting.getCirculationStatusSetting().validate(this);
        appSetting.getAttributes().forEach(attribute -> attribute.validate(this));
        appSetting.getOperationMenuItems().forEach(item -> item.validate(this));
    }

    private void validateHomePageExists() {
        String homePageId = appSetting.getConfig().getHomePageId();

        if (!allPageIds.contains(homePageId)) {
            throw new MryException(NO_APP_HOME_PAGE, "应用首页不存在。",
                    mapOf("homePageId", homePageId));
        }
    }

    private void validatePageIdsNoDuplication() {
        if (allPageIds.size() != appSetting.getPages().size()) {
            throw new MryException(PAGE_ID_DUPLICATED, "应用下所有页面ID不能重复。");
        }
    }

    private void validateAttributeIdsNoDuplication() {
        if (allAttributeIds.size() != appSetting.getAttributes().size()) {
            throw new MryException(ATTRIBUTE_ID_DUPLICATED, "应用下所有自定义属性ID不能重复。");
        }
    }

    private void validateAttributeNamesNoDuplication() {
        Set<String> attributeNames = appSetting.getAttributes().stream().map(Attribute::getName)
                .collect(toImmutableSet());

        if (attributeNames.size() != appSetting.getAttributes().size()) {
            throw new MryException(ATTRIBUTE_NAME_DUPLICATED, "自定义属性名不能重复。");
        }
    }

    private void validateAllControlIdsNoDuplication() {
        List<String> controlIds = appSetting.getPages().stream()
                .flatMap(page -> page.getControls().stream().map(Control::getId))
                .collect(toImmutableList());

        if (controlIds.size() != Set.copyOf(controlIds).size()) {
            throw new MryException(CONTROL_ID_DUPLICATED, "应用下所有的控件ID不能重复。");
        }
    }

    private void validateOperationMenuItemsNoDuplication() {
        if (isDuplicated(appSetting.getOperationMenuItems())) {
            throw new MryException(OPERATION_MENU_ITEM_ID_DUPLICATED, "应用下所有运营菜单项ID不能重复。");
        }

        Set<String> menuItemSchemas = appSetting.getOperationMenuItems().stream()
                .map(OperationMenuItem::schema)
                .collect(toImmutableSet());
        if (menuItemSchemas.size() != appSetting.getOperationMenuItems().size()) {
            throw new MryException(OPERATION_MENU_ITEM_DUPLICATED, "不能存在相同引用的运营菜单项。");
        }

        Set<String> itemNames = appSetting.getOperationMenuItems().stream()
                .map(OperationMenuItem::getName)
                .collect(toImmutableSet());
        if (itemNames.size() != appSetting.getOperationMenuItems().size()) {
            throw new MryException(OPERATION_MENU_ITEM_NAME_DUPLICATED, "应用下所有运营菜单项名称不能重复。");
        }
    }

    public void validateControlTypesNotModified(AppSettingContext newContext) {
        Map<String, Control> newControls = newContext.allControls;
        for (String controlId : newControls.keySet()) {
            Control oldControl = allControls.get(controlId);
            if (oldControl != null && newControls.get(controlId).getType() != oldControl.getType()) {
                throw new MryException(CONTROL_TYPE_NOT_MATCH, "控件类型不能变更。", mapOf("controlId", controlId));
            }
        }
    }

    public void validateAttributesSchemaNotModified(AppSettingContext newContext) {
        Map<String, Attribute> newAttributesMap = newContext.allAttributes;
        for (String attributeId : newAttributesMap.keySet()) {
            Attribute oldAttribute = allAttributes.get(attributeId);
            if (oldAttribute != null && newAttributesMap.get(attributeId).isSchemaDifferentFrom(oldAttribute)) {
                throw new MryException(ATTRIBUTE_SCHEMA_CANNOT_MODIFIED, "自定义属性不能变更。", mapOf("attributeId", attributeId));
            }
        }
    }

    public Set<ControlType> calculateNewlyAddedControlTypes(AppSettingContext newContext) {
        Set<ControlType> oldControlTypes = this.allControls.values().stream()
                .map(Control::getType)
                .collect(toImmutableSet());

        Set<ControlType> newControlTypes = new HashSet<>(newContext.allControls.values().stream()
                .map(Control::getType)
                .collect(toImmutableSet()));

        newControlTypes.removeAll(oldControlTypes);
        return newControlTypes;
    }

    public Set<PageInfo> calculateDeletedPages(AppSettingContext newContext) {
        Set<String> oldPageIds = new HashSet<>(allPageIds);
        oldPageIds.removeAll(newContext.allPageIds);
        return oldPageIds.stream().map(id -> allPages.get(id).toPageInfo()).collect(toImmutableSet());
    }

    public Set<ControlInfo> calculateDeletedControls(AppSettingContext newContext, Set<String> excludePageIds) {
        Set<ControlInfo> oldControls = new HashSet<>(this.allControlInfos.values());
        oldControls.removeAll(newContext.allControlInfos.values());
        return oldControls.stream().filter(control -> !excludePageIds.contains(control.getPageId()))
                .collect(toImmutableSet());
    }

    public Set<TextOptionInfo> calculateDeletedTextOptions(AppSettingContext newContext, Set<String> excludeControlIds) {
        Set<TextOptionInfo> oldOptions = new HashSet<>(this.allTextOptionsInfo());
        oldOptions.removeAll(newContext.allTextOptionsInfo());

        return oldOptions.stream().filter(info -> !excludeControlIds.contains(info.getControlId()))
                .collect(toImmutableSet());
    }

    private Set<TextOptionInfo> allTextOptionsInfo() {
        return this.allControls.values().stream()
                .filter(control -> control instanceof AbstractTextOptionControl)
                .map(control -> ((AbstractTextOptionControl) control).optionsInfo())
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(toImmutableSet());
    }

    public AttributeCheckChangeResult calculateAttributeChanges(AppSettingContext newContext) {
        Set<AttributeInfo> newAttributeInfos = newContext.allAttributes.values().stream()
                .map(Attribute::toInfo).collect(toImmutableSet());

        Set<AttributeInfo> oldAttributeInfos = this.allAttributes.values().stream()
                .map(Attribute::toInfo).collect(toImmutableSet());

        Set<AttributeInfo> created = calculateAttributeDifference(newAttributeInfos, oldAttributeInfos);
        Set<AttributeInfo> deleted = calculateAttributeDifference(oldAttributeInfos, newAttributeInfos);

        return AttributeCheckChangeResult.builder()
                .createdAttributes(created)
                .deletedAttributes(deleted)
                .build();
    }

    private Set<AttributeInfo> calculateAttributeDifference(Set<AttributeInfo> oldA, Set<AttributeInfo> newA) {
        Set<AttributeInfo> newIds = new HashSet<>(oldA);
        newIds.removeAll(newA);
        return newIds;
    }

    public Set<String> calculateSubmitTypeChanges(AppSettingContext newContext, SubmitType submitType) {
        Set<String> changedPageIds = new HashSet<>();
        for (Map.Entry<String, Page> entry : newContext.allPages.entrySet()) {
            Page oldPage = this.allPages.get(entry.getKey());
            if (oldPage != null
                    && oldPage.getSetting().getSubmitType() != submitType
                    && entry.getValue().getSetting().getSubmitType() == submitType) {
                changedPageIds.add(oldPage.getId());
            }
        }
        return Set.copyOf(changedPageIds);
    }

    public boolean pageNotExists(String pageId) {
        return !allPageIds.contains(pageId);
    }

    public boolean attributeNotExists(String attributeId) {
        return !allAttributeIds.contains(attributeId);
    }

    public boolean controlNotExists(String pageId, String controlId) {
        return allControlInfos.values().stream()
                .noneMatch(controlInfo -> controlInfo.getPageId().equals(pageId) && controlInfo.getControlId().equals(controlId));
    }

    public ControlType controlTypeOf(String controlId) {
        return allControlInfos.get(controlId).getControlType();
    }

    public boolean isGeolocationEnabled() {
        return appSetting.getConfig().isGeolocationEnabled();
    }

    public Page pageForControl(String controlId) {
        return allPages.get(allControlInfos.get(controlId).getPageId());
    }

    public Optional<Attribute> attributeByIdOptional(String attributeId) {
        return Optional.ofNullable(allAttributes.get(attributeId));
    }

    public Attribute attributeById(String attributeId) {
        Attribute attribute = allAttributes.get(attributeId);
        if (attribute == null) {
            throw new MryException(ATTRIBUTE_NOT_FOUND, "属性项不存在。", mapOf("attributeId", attributeId));
        }
        return attribute;
    }

    public Optional<Control> controlByIdOptional(String controlId) {
        return Optional.ofNullable(allControls.get(controlId));
    }

    public Control controlById(String controlId) {
        Control control = allControls.get(controlId);
        if (control == null) {
            throw new MryException(CONTROL_NOT_FOUND, "控件不存在。", mapOf("controlId", controlId));
        }
        return control;
    }

}
