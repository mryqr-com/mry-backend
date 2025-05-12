package com.mryqr.core.platetemplate;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.CreateAppResponse;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.plate.PlateSetting;
import com.mryqr.core.app.domain.plate.PlateTextValue;
import com.mryqr.core.app.domain.plate.control.SingleRowTextControl;
import com.mryqr.core.login.LoginApi;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.platetemplate.command.CreatePlateTemplateCommand;
import com.mryqr.core.platetemplate.command.UpdatePlateTemplateCommand;
import com.mryqr.core.platetemplate.domain.PlateTemplate;
import com.mryqr.core.platetemplate.query.QListPlateTemplate;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeType.DIRECT_INPUT;
import static com.mryqr.core.app.domain.plate.PlateTextValueType.QR_ATTRIBUTE;
import static com.mryqr.core.app.domain.plate.PlateTextValueType.QR_PROPERTY;
import static com.mryqr.core.app.domain.plate.control.PlateControlType.SINGLE_ROW_TEXT;
import static com.mryqr.core.app.domain.ui.align.HorizontalAlignType.CENTER;
import static com.mryqr.core.app.domain.ui.borderradius.BorderRadius.noBorderRadius;
import static com.mryqr.management.apptemplate.MryAppTemplateManageApp.MRY_APP_TEMPLATE_MANAGE_APP_ID;
import static com.mryqr.management.apptemplate.MryAppTemplateTenant.ADMIN_INIT_MOBILE;
import static com.mryqr.management.apptemplate.MryAppTemplateTenant.ADMIN_INIT_PASSWORD;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.*;

class PlateTemplateControllerApiTest extends BaseApiTest {

    @Test
    public void should_create_plate_template() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);

        App app = appRepository.byId(MRY_APP_TEMPLATE_MANAGE_APP_ID);
        CreatePlateTemplateCommand command = CreatePlateTemplateCommand.builder()
                .appId(app.getId())
                .plateSetting(app.getSetting().getPlateSetting())
                .build();

        String plateTemplateId = PlateTemplateApi.createPlateTemplate(jwt, command);
        PlateTemplate plateTemplate = plateTemplateRepository.byId(plateTemplateId);
        assertEquals(app.getSetting().getPlateSetting(), plateTemplate.getPlateSetting());
    }

    @Test
    public void should_fail_create_plate_template_if_setting_not_complete() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        App app = appRepository.byId(MRY_APP_TEMPLATE_MANAGE_APP_ID);
        AppSetting setting = app.getSetting();
        PlateSetting plateSetting = setting.getPlateSetting();

        SingleRowTextControl singleRowTextControl = SingleRowTextControl.builder()
                .id(newShortUuid())
                .type(SINGLE_ROW_TEXT)
                .borderRadius(noBorderRadius())
                .border(rBorder())
                .textValue(PlateTextValue.builder()
                        .type(QR_PROPERTY)
                        .build())
                .alignType(CENTER)
                .fontStyle(rFontStyle())
                .height(50)
                .logoHeight(30)
                .build();
        plateSetting.getControls().clear();
        plateSetting.getControls().add(singleRowTextControl);

        CreatePlateTemplateCommand command = CreatePlateTemplateCommand.builder()
                .appId(app.getId())
                .plateSetting(app.getSetting().getPlateSetting())
                .build();

        assertError(() -> PlateTemplateApi.createPlateTemplateRaw(jwt, command), PLATE_SETTING_NOT_COMPLETE);
    }

    @Test
    public void should_fail_create_plate_template_if_exist_reference_to_attribute() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        CreateAppResponse response = AppApi.createApp(jwt);

        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(DIRECT_INPUT).build();
        AppApi.updateAppAttributes(jwt, response.getAppId(), attribute);

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        PlateSetting plateSetting = setting.getPlateSetting();

        SingleRowTextControl singleRowTextControl = SingleRowTextControl.builder()
                .id(newShortUuid())
                .type(SINGLE_ROW_TEXT)
                .borderRadius(noBorderRadius())
                .border(rBorder())
                .textValue(PlateTextValue.builder()
                        .type(QR_ATTRIBUTE)
                        .attributeId(attribute.getId())
                        .build())
                .alignType(CENTER)
                .fontStyle(rFontStyle())
                .height(50)
                .logoHeight(30)
                .build();
        plateSetting.getControls().clear();
        plateSetting.getControls().add(singleRowTextControl);

        CreatePlateTemplateCommand command = CreatePlateTemplateCommand.builder()
                .appId(app.getId())
                .plateSetting(app.getSetting().getPlateSetting())
                .build();

        assertError(() -> PlateTemplateApi.createPlateTemplateRaw(jwt, command), PLATE_SETTING_HAS_ATTRIBUTES);
    }

    @Test
    public void should_list_all_plate_templates() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);

        App app = appRepository.byId(MRY_APP_TEMPLATE_MANAGE_APP_ID);
        CreateAppResponse anotherAppResponse = AppApi.createApp(jwt);

        App anotherApp = appRepository.byId(anotherAppResponse.getAppId());
        String plateTemplateId = PlateTemplateApi.createPlateTemplate(jwt, CreatePlateTemplateCommand.builder()
                .appId(app.getId())
                .plateSetting(app.getSetting().getPlateSetting())
                .build());

        String anotherPlateTemplateId = PlateTemplateApi.createPlateTemplate(jwt, CreatePlateTemplateCommand.builder()
                .appId(anotherApp.getId())
                .plateSetting(app.getSetting().getPlateSetting())
                .build());

        assertTrue(PlateTemplateApi.listPlateTemplates(jwt)
                .stream().map(QListPlateTemplate::getId).toList()
                .containsAll(List.of(plateTemplateId, anotherPlateTemplateId)));

        CreateMemberResponse aMember = MemberApi.createMemberAndLogin(jwt);
        assertTrue(PlateTemplateApi.listPlateTemplates(aMember.getJwt())
                .stream().map(QListPlateTemplate::getId).toList()
                .containsAll(List.of(plateTemplateId, anotherPlateTemplateId)));
    }

    @Test
    public void should_delete_plate_template() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);

        App app = appRepository.byId(MRY_APP_TEMPLATE_MANAGE_APP_ID);
        CreatePlateTemplateCommand command = CreatePlateTemplateCommand.builder()
                .appId(app.getId())
                .plateSetting(app.getSetting().getPlateSetting())
                .build();

        String plateTemplateId = PlateTemplateApi.createPlateTemplate(jwt, command);
        PlateTemplate plateTemplate = plateTemplateRepository.byId(plateTemplateId);
        assertNotNull(plateTemplate);

        PlateTemplateApi.deletePlateTemplate(jwt, plateTemplateId);
        assertTrue(plateTemplateRepository.byIdOptional(plateTemplateId).isEmpty());
    }

    @Test
    public void non_mry_self_tenant_should_not_create_plate_template() {
        PreparedAppResponse response = setupApi.registerWithApp();
        App app = appRepository.byId(response.getAppId());
        CreatePlateTemplateCommand command = CreatePlateTemplateCommand.builder()
                .appId(app.getId())
                .plateSetting(app.getSetting().getPlateSetting())
                .build();

        assertError(() -> PlateTemplateApi.createPlateTemplateRaw(response.getJwt(), command), ACCESS_DENIED);
    }

    @Test
    public void should_update_plate_template_image() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);

        App app = appRepository.byId(MRY_APP_TEMPLATE_MANAGE_APP_ID);
        CreatePlateTemplateCommand command = CreatePlateTemplateCommand.builder()
                .appId(app.getId())
                .plateSetting(app.getSetting().getPlateSetting())
                .build();

        String plateTemplateId = PlateTemplateApi.createPlateTemplate(jwt, command);
        UploadedFile image = rImageFile();
        PlateTemplateApi.updatePlateTemplate(jwt, plateTemplateId, UpdatePlateTemplateCommand.builder().image(image).build());

        PlateTemplate plateTemplate = plateTemplateRepository.byId(plateTemplateId);
        assertEquals(image, plateTemplate.getImage());
    }

}