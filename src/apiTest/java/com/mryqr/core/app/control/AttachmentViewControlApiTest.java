package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PAttachmentViewControl;
import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.core.common.exception.ErrorCode.ATTACHMENT_ID_DUPLICATED;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.utils.RandomTestFixture.defaultAttachmentViewControl;
import static com.mryqr.utils.RandomTestFixture.defaultAttachmentViewControlBuilder;
import static com.mryqr.utils.RandomTestFixture.rImageFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AttachmentViewControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        PAttachmentViewControl control = defaultAttachmentViewControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_fail_create_control_if_attachment_id_duplicated() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        UploadedFile uploadedFile = rImageFile();
        PAttachmentViewControl control = defaultAttachmentViewControlBuilder().attachments(newArrayList(uploadedFile, uploadedFile)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting), ATTACHMENT_ID_DUPLICATED);
    }

}
