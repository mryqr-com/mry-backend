package com.mryqr.core.app.domain;

import com.mryqr.common.domain.TextOption;
import com.mryqr.common.domain.permission.Permission;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.circulation.CirculationStatusSetting;
import com.mryqr.core.app.domain.config.AppConfig;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.app.domain.page.control.FRadioControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.header.PageHeader;
import com.mryqr.core.app.domain.page.menu.Menu;
import com.mryqr.core.app.domain.page.setting.SubmitType;
import com.mryqr.core.app.domain.plate.*;
import com.mryqr.core.app.domain.plate.control.KeyValueControl;
import com.mryqr.core.app.domain.plate.control.SingleRowTextControl;
import com.mryqr.core.app.domain.ui.FontStyle;
import com.mryqr.core.app.domain.ui.MinMaxSetting;
import com.mryqr.core.app.domain.ui.border.Border;
import com.mryqr.core.app.domain.ui.borderradius.BorderRadius;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.ItemStatusAttributeValue;
import com.mryqr.core.qr.domain.attribute.MultiLineTextAttributeValue;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.mryqr.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.common.domain.permission.Permission.CAN_MANAGE_GROUP;
import static com.mryqr.common.exception.ErrorCode.APP_TEMPLATE_NOT_PUBLISHED;
import static com.mryqr.common.exception.ErrorCode.APP_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.AppTopBar.defaultAppTopBar;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CREATE_TIME;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CREATOR;
import static com.mryqr.core.app.domain.config.AppLandingPageType.DEFAULT;
import static com.mryqr.core.app.domain.operationmenu.OperationMenuItem.defaultOperationMenuItems;
import static com.mryqr.core.app.domain.page.Page.newPageId;
import static com.mryqr.core.app.domain.page.control.Control.newControlId;
import static com.mryqr.core.app.domain.page.control.ControlFillableSetting.defaultControlFillableSetting;
import static com.mryqr.core.app.domain.page.control.ControlNameSetting.defaultControlNameSetting;
import static com.mryqr.core.app.domain.page.control.ControlStyleSetting.defaultControlStyleSetting;
import static com.mryqr.core.app.domain.page.control.ControlType.*;
import static com.mryqr.core.app.domain.page.setting.PageSetting.defaultPageSetting;
import static com.mryqr.core.app.domain.page.submitbutton.SubmitButton.defaultSubmitButton;
import static com.mryqr.core.app.domain.page.title.PageTitle.defaultPageTitle;
import static com.mryqr.core.app.domain.plate.PlateQrPropertyType.*;
import static com.mryqr.core.app.domain.plate.PlateSize.MM_70x50;
import static com.mryqr.core.app.domain.plate.PlateTextValueType.QR_PROPERTY;
import static com.mryqr.core.app.domain.plate.control.PlateControlType.KEY_VALUE;
import static com.mryqr.core.app.domain.plate.control.PlateControlType.SINGLE_ROW_TEXT;
import static com.mryqr.core.app.domain.ui.BoxedTextStyle.defaultControlDescriptionStyle;
import static com.mryqr.core.app.domain.ui.align.HorizontalAlignType.JUSTIFY;
import static com.mryqr.core.app.domain.ui.align.HorizontalPositionType.RIGHT;
import static com.mryqr.core.app.domain.ui.align.VerticalAlignType.MIDDLE;
import static com.mryqr.core.app.domain.ui.border.Border.noBorder;
import static com.mryqr.core.app.domain.ui.border.BorderSide.BOTTOM;
import static com.mryqr.core.app.domain.ui.border.BorderType.SOLID;
import static com.mryqr.core.app.domain.ui.borderradius.BorderRadius.BorderRadiusCorner.TOP_LEFT;
import static com.mryqr.core.app.domain.ui.borderradius.BorderRadius.BorderRadiusCorner.TOP_RIGHT;
import static com.mryqr.core.app.domain.ui.borderradius.BorderRadius.noBorderRadius;
import static com.mryqr.management.apptemplate.MryAppTemplateManageApp.*;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@RequiredArgsConstructor
public class AppFactory {
    private final AppHeaderImageProvider appHeaderImageProvider;
    private final AppRepository appRepository;

    public CreateAppResult create(String name, User user) {
        checkNameDuplication(name, user.getTenantId());

        Page homePage = Page.builder()
                .id(newPageId())
                .header(PageHeader.defaultPageHeaderBuilder().image(appHeaderImageProvider.defaultAppHeaderImage()).build())
                .title(defaultPageTitle())
                .controls(List.of(defaultFCheckboxControl(), defaultFRadioControl(), defaultFSingleLineTextControl()))
                .submitButton(defaultSubmitButton())
                .setting(defaultPageSetting())
                .build();

        Menu menu = Menu.builder().links(List.of()).showBasedOnPermission(false).build();

        AppSetting setting = AppSetting.builder()
                .config(AppConfig.builder()
                        .operationPermission(CAN_MANAGE_GROUP)
                        .landingPageType(DEFAULT)
                        .qrWebhookTypes(List.of())
                        .homePageId(homePage.getId())
                        .build())
                .appTopBar(defaultAppTopBar())
                .pages(List.of(homePage))
                .menu(menu)
                .attributes(defaultAttributes())
                .operationMenuItems(defaultOperationMenuItems())
                .plateSetting(defaultPlateSetting())
                .circulationStatusSetting(CirculationStatusSetting.create())
                .build();

        App app = new App(name, setting, user);
        Group group = new Group("默认分组", app, user);
        GroupHierarchy groupHierarchy = new GroupHierarchy(app, group.getId(), user);

        return CreateAppResult.builder()
                .app(app)
                .defaultGroup(group)
                .groupHierarchy(groupHierarchy)
                .build();
    }

    private PlateSetting defaultPlateSetting() {
        PlateConfig plateConfig = PlateConfig.builder()
                .size(MM_70x50)
                .width(560)
                .height(400)
                .borderRadius(10)
                .build();

        SingleRowTextControl tenantNameControl = SingleRowTextControl.builder()
                .id(newShortUuid())
                .type(SINGLE_ROW_TEXT)
                .border(noBorder())
                .borderRadius(BorderRadius.builder().corners(Set.of(TOP_LEFT, TOP_RIGHT)).radius(10).build())
                .marginTop(10)
                .marginBottom(10)
                .marginLeft(20)
                .marginRight(20)
                .textValue(PlateTextValue.builder().type(QR_PROPERTY).propertyType(TENANT_NAME).build())
                .fontStyle(FontStyle.builder().bold(true).color("rgba(0, 0, 0, 1)").fontFamily("黑体").italic(false).fontSize(36).build())
                .alignType(JUSTIFY)
                .letterSpacing(0)
                .height(60)
                .logoHeight(30)
                .logoTextSpacing(5)
                .build();

        SingleRowTextControl appNameControl = SingleRowTextControl.builder()
                .id(newShortUuid())
                .type(SINGLE_ROW_TEXT)
                .border(Border.builder().color("rgba(136, 136, 136, 1)").sides(Set.of(BOTTOM)).width(2).type(SOLID).build())
                .borderRadius(noBorderRadius())
                .marginTop(5)
                .marginBottom(10)
                .marginLeft(20)
                .marginRight(20)
                .paddingLeft(10)
                .paddingRight(10)
                .textValue(PlateTextValue.builder().type(QR_PROPERTY).propertyType(APP_NAME).build())
                .fontStyle(FontStyle.builder().bold(false).color("rgba(48, 49, 51, 1)").fontFamily("黑体").italic(false).fontSize(26).build())
                .alignType(JUSTIFY)
                .letterSpacing(0)
                .height(40)
                .logoHeight(30)
                .logoTextSpacing(5)
                .build();

        KeyValueControl keyValueControl = KeyValueControl.builder()
                .id(newShortUuid())
                .type(KEY_VALUE)
                .border(noBorder())
                .borderRadius(noBorderRadius())
                .marginTop(15)
                .marginBottom(10)
                .marginLeft(20)
                .marginRight(20)
                .textValues(List.of(PlateNamedTextValue.builder().id(newShortUuid()).name("名称")
                                .value(PlateTextValue.builder().type(QR_PROPERTY).propertyType(QR_NAME).build()).build(),
                        PlateNamedTextValue.builder().id(newShortUuid()).name("分组")
                                .value(PlateTextValue.builder().type(QR_PROPERTY).propertyType(QR_GROUP_NAME).build()).build()))
                .fontStyle(FontStyle.builder().bold(false).color("rgba(48, 49, 51, 1)").fontFamily("黑体").italic(false).fontSize(26).build())
                .lineHeight(50)
                .textHorizontalAlignType(JUSTIFY)
                .verticalAlignType(MIDDLE)
                .horizontalPositionType(RIGHT)
                .horizontalGutter(15)
                .qrEnabled(true)
                .qrImageSetting(PlateQrImageSetting.builder()
                        .width(220)
                        .margin(2)
                        .color("rgba(0, 0, 0, 1)")
                        .build())
                .build();

        return PlateSetting.builder()
                .config(plateConfig)
                .controls(List.of(tenantNameControl, appNameControl, keyValueControl))
                .build();
    }

    public CreateAppResult copyFrom(App sourceApp, String name, User user) {
        checkNameDuplication(name, user.getTenantId());

        App copiedApp = new App(name, sourceApp, user);
        Group group = new Group("默认分组", copiedApp, user);
        GroupHierarchy groupHierarchy = new GroupHierarchy(copiedApp, group.getId(), user);
        return CreateAppResult.builder()
                .app(copiedApp)
                .defaultGroup(group)
                .groupHierarchy(groupHierarchy)
                .build();
    }

    public CreateAppResult createFromTemplate(QR appTemplate, App templateApp, User user) {
        checkNameDuplication(appTemplate.getName(), user.getTenantId());

        ItemStatusAttributeValue templateStatus = (ItemStatusAttributeValue) appTemplate.getAttributeValues().get(STATUS_ATTRIBUTE_ID);
        if (templateStatus == null || !Objects.equals(templateStatus.getOptionId(), PUBLISHED_STATUS_OPTION_ID)) {
            throw new MryException(APP_TEMPLATE_NOT_PUBLISHED, "创建应用失败，所使用模板尚未发布。", mapOf("appTemplateId", appTemplate.getId()));
        }

        MultiLineTextAttributeValue modifiers = (MultiLineTextAttributeValue) appTemplate.getAttributeValues().get(PAGE_MODIFIER_ATTRIBUTE_ID);
        if (modifiers != null) {
            Map<String, Pair<SubmitType, Permission>> typeAndPermissionModifiers = parseTypeAndPermissionModifier(modifiers);
            if (isNotEmpty(typeAndPermissionModifiers)) {
                templateApp.modifyPagesSetting(typeAndPermissionModifiers);
            }
        }

        App app = new App(appTemplate, templateApp, user);
        Group group = new Group("默认分组", app, user);
        GroupHierarchy groupHierarchy = new GroupHierarchy(app, group.getId(), user);
        return CreateAppResult.builder()
                .app(app)
                .defaultGroup(group)
                .groupHierarchy(groupHierarchy)
                .build();
    }

    public CreateAppResult create(String id, String name, AppSetting setting, String defaultGroupId, User user) {
        checkNameDuplication(name, user.getTenantId());

        App app = new App(id, name, setting, user);
        Group group = new Group(defaultGroupId, "默认分组", app, user);
        GroupHierarchy groupHierarchy = new GroupHierarchy(app, group.getId(), user);
        return CreateAppResult.builder()
                .app(app)
                .defaultGroup(group)
                .groupHierarchy(groupHierarchy)
                .build();
    }

    private Map<String, Pair<SubmitType, Permission>> parseTypeAndPermissionModifier(MultiLineTextAttributeValue modifiers) {
        String content = modifiers.getContent();

        if (isBlank(content)) {
            return Map.of();
        }

        String[] lines = content.split("\n");
        Map<String, Pair<SubmitType, Permission>> result = new HashMap<>();
        for (String line : lines) {
            String[] split = deleteWhitespace(line).split(":|：");
            if (split.length != 3) {
                break;
            }

            try {
                String pageId = split[0];
                SubmitType submitType = SubmitType.valueOf(split[1]);
                Permission permission = Permission.valueOf(split[2]);
                result.put(pageId, Pair.of(submitType, permission));
            } catch (Throwable t) {
                break;
            }
        }

        return result;
    }

    private List<Attribute> defaultAttributes() {
        Attribute instanceCreatorAttribute = Attribute.builder()
                .id(newAttributeId())
                .name("创建人")
                .type(INSTANCE_CREATOR)
                .pcListEligible(true)
                .build();

        Attribute instanceCreateTimeAttribute = Attribute.builder()
                .id(newAttributeId())
                .name("创建时间")
                .type(INSTANCE_CREATE_TIME)
                .pcListEligible(true)
                .build();

        return List.of(instanceCreatorAttribute, instanceCreateTimeAttribute);
    }

    private FCheckboxControl defaultFCheckboxControl() {
        TextOption option1 = TextOption.builder().id(newShortUuid()).name("选项一").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name("选项二").build();
        return FCheckboxControl.builder()
                .id(newControlId())
                .type(CHECKBOX)
                .name("示例多选")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .fillableSetting(defaultControlFillableSetting())
                .styleSetting(defaultControlStyleSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .options(List.of(option1, option2))
                .minMaxSetting(MinMaxSetting.builder().max(20).min(0).build())
                .build();
    }

    private FRadioControl defaultFRadioControl() {
        TextOption option1 = TextOption.builder().id(newShortUuid()).name("选项一").build();
        TextOption option2 = TextOption.builder().id(newShortUuid()).name("选项二").build();
        return FRadioControl.builder()
                .id(newControlId())
                .type(RADIO)
                .name("示例单选")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .fillableSetting(defaultControlFillableSetting())
                .styleSetting(defaultControlStyleSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .options(List.of(option1, option2))
                .build();
    }

    private FSingleLineTextControl defaultFSingleLineTextControl() {
        return FSingleLineTextControl.builder()
                .id(newControlId())
                .type(SINGLE_LINE_TEXT)
                .name("示例输入框")
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .fillableSetting(defaultControlFillableSetting())
                .styleSetting(defaultControlStyleSetting())
                .permissionEnabled(false)
                .permission(CAN_MANAGE_APP)
                .minMaxSetting(MinMaxSetting.builder().min(0).max(50).build())
                .build();
    }

    private void checkNameDuplication(String name, String tenantId) {
        if (appRepository.cachedExistsByName(name, tenantId)) {
            throw new MryException(APP_WITH_NAME_ALREADY_EXISTS, "创建应用失败，应用名【" + name + "】与已有应用重复。", mapOf("name", name));
        }
    }
}
