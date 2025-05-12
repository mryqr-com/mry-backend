package com.mryqr.core.app.plate;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.plate.*;
import com.mryqr.core.app.domain.plate.control.*;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static com.mryqr.common.exception.ErrorCode.PLATE_CONTROL_ID_DUPLICATED;
import static com.mryqr.common.exception.ErrorCode.VALIDATION_ATTRIBUTE_NOT_EXIST;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeType.DIRECT_INPUT;
import static com.mryqr.core.app.domain.plate.PlateQrPropertyType.QR_NAME;
import static com.mryqr.core.app.domain.plate.PlateSize.MM_60x60;
import static com.mryqr.core.app.domain.plate.PlateSize.MM_60x90;
import static com.mryqr.core.app.domain.plate.PlateTextValueType.*;
import static com.mryqr.core.app.domain.plate.control.PlateControlType.*;
import static com.mryqr.core.app.domain.ui.align.HorizontalAlignType.CENTER;
import static com.mryqr.core.app.domain.ui.align.HorizontalAlignType.JUSTIFY;
import static com.mryqr.core.app.domain.ui.align.HorizontalPositionType.RIGHT;
import static com.mryqr.core.app.domain.ui.align.VerticalAlignType.MIDDLE;
import static com.mryqr.core.app.domain.ui.borderradius.BorderRadius.defaultBorderRadius;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlateSettingApiTest extends BaseApiTest {

    @Test
    public void update_app_setting_should_also_update_plate_setting() {
        PreparedAppResponse response = setupApi.registerWithApp();
        Attribute attribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(DIRECT_INPUT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        PlateSetting plateSetting = setting.getPlateSetting();

        PlateConfig config = plateSetting.getConfig();
        ReflectionTestUtils.setField(config, "size", MM_60x90);

        KeyValueControl keyValueControl = KeyValueControl.builder()
                .id(newShortUuid())
                .type(KEY_VALUE)
                .borderRadius(defaultBorderRadius())
                .border(rBorder())
                .textValues(List.of(PlateNamedTextValue.builder()
                        .id(newShortUuid())
                        .name(rPlateKeyName())
                        .value(PlateTextValue.builder()
                                .type(QR_PROPERTY)
                                .propertyType(QR_NAME)
                                .build())
                        .build()))
                .fontStyle(rFontStyle())
                .lineHeight(50)
                .textHorizontalAlignType(JUSTIFY)
                .verticalAlignType(MIDDLE)
                .horizontalPositionType(RIGHT)
                .horizontalGutter(0)
                .qrEnabled(true)
                .qrImageSetting(PlateQrImageSetting.builder()
                        .width(500)
                        .build())
                .build();

        QrImageControl qrImageControl = QrImageControl.builder()
                .id(newShortUuid())
                .type(QR_IMAGE)
                .borderRadius(defaultBorderRadius())
                .border(rBorder())
                .setting(PlateQrImageSetting.builder()
                        .width(500)
                        .build())
                .alignType(CENTER)
                .build();

        SingleRowTextControl singleRowTextControl = SingleRowTextControl.builder()
                .id(newShortUuid())
                .type(SINGLE_ROW_TEXT)
                .borderRadius(defaultBorderRadius())
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

        TableControl tableControl = TableControl.builder()
                .id(newShortUuid())
                .type(TABLE)
                .borderRadius(defaultBorderRadius())
                .border(rBorder())
                .headerEnabled(true)
                .headerTextValue(PlateTextValue.builder()
                        .type(FIXED_TEXT)
                        .text("表头")
                        .build())
                .headerFontStyle(rFontStyle())
                .headerHeight(50)
                .headerAlignType(CENTER)
                .contentTextValues(List.of(PlateNamedTextValue.builder()
                        .id(newShortUuid())
                        .name(rPlateKeyName())
                        .value(PlateTextValue.builder()
                                .type(QR_PROPERTY)
                                .propertyType(QR_NAME)
                                .build())
                        .build()))
                .contentFontStyle(rFontStyle())
                .cellHeight(30)
                .borderWidth(1)
                .qrEnabled(true)
                .qrImageSetting(PlateQrImageSetting.builder()
                        .width(500)
                        .build())
                .qrRows(3)
                .build();

        HeaderImageControl headerImageControl = HeaderImageControl.builder()
                .id(newShortUuid())
                .type(HEADER_IMAGE)
                .borderRadius(defaultBorderRadius())
                .border(rBorder())
                .alignType(JUSTIFY)
                .width(100)
                .height(100)
                .build();

        plateSetting.getControls().clear();
        plateSetting.getControls().add(keyValueControl);
        plateSetting.getControls().add(qrImageControl);
        plateSetting.getControls().add(singleRowTextControl);
        plateSetting.getControls().add(tableControl);
        plateSetting.getControls().add(headerImageControl);
        AppApi.updateAppSetting(response.getJwt(), appId, app.getVersion(), setting);

        App updatedApp = appRepository.byId(appId);
        PlateSetting updatedSetting = updatedApp.getSetting().getPlateSetting();
        assertEquals(480, updatedSetting.getConfig().getWidth());
        assertEquals(720, updatedSetting.getConfig().getHeight());
        List<PlateControl> controls = updatedSetting.getControls();
        assertEquals(5, controls.size());
        assertTrue(controls.get(0).isComplete());
        assertTrue(controls.get(1).isComplete());
        assertTrue(controls.get(2).isComplete());
        assertTrue(controls.get(3).isComplete());
        assertTrue(controls.get(4).isComplete());
    }

    @Test
    public void should_create_default_control_if_no_control() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        PlateSetting plateSetting = setting.getPlateSetting();
        plateSetting.getControls().clear();

        AppApi.updateAppSetting(response.getJwt(), appId, app.getVersion(), setting);
        App updatedApp = appRepository.byId(appId);
        PlateSetting updatedSetting = updatedApp.getSetting().getPlateSetting();
        assertEquals(MM_60x60, updatedSetting.getConfig().getSize());
        PlateControl plateControl = updatedSetting.getControls().get(0);
        assertEquals(QR_IMAGE, plateControl.getType());
    }

    @Test
    public void should_fail_update_plate_setting_if_control_id_duplicated() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        PlateSetting plateSetting = setting.getPlateSetting();
        plateSetting.getControls().clear();

        String id = newShortUuid();
        QrImageControl qrImageControl1 = QrImageControl.builder()
                .id(id)
                .type(QR_IMAGE)
                .borderRadius(defaultBorderRadius())
                .border(rBorder())
                .setting(PlateQrImageSetting.builder()
                        .width(500)
                        .build())
                .alignType(CENTER)
                .build();

        QrImageControl qrImageControl2 = QrImageControl.builder()
                .id(id)
                .type(QR_IMAGE)
                .borderRadius(defaultBorderRadius())
                .border(rBorder())
                .setting(PlateQrImageSetting.builder()
                        .width(500)
                        .build())
                .alignType(CENTER)
                .build();

        plateSetting.getControls().addAll(List.of(qrImageControl1, qrImageControl2));
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), PLATE_CONTROL_ID_DUPLICATED);
    }

    @Test
    public void should_fail_create_plate_setting_if_attribute_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        PlateSetting plateSetting = setting.getPlateSetting();
        plateSetting.getControls().clear();

        SingleRowTextControl singleRowTextControl = SingleRowTextControl.builder()
                .id(newShortUuid())
                .type(SINGLE_ROW_TEXT)
                .borderRadius(defaultBorderRadius())
                .border(rBorder())
                .textValue(PlateTextValue.builder()
                        .type(QR_ATTRIBUTE)
                        .attributeId(newAttributeId())
                        .build())
                .alignType(CENTER)
                .fontStyle(rFontStyle())
                .height(50)
                .logoHeight(30)
                .build();

        plateSetting.getControls().add(singleRowTextControl);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), VALIDATION_ATTRIBUTE_NOT_EXIST);
    }

}
