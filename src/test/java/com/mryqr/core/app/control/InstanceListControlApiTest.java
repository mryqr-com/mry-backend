package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PInstanceListControl;
import com.mryqr.core.presentation.PresentationApi;
import com.mryqr.core.presentation.query.instancelist.QInstanceListPresentation;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;

import static com.mryqr.utils.RandomTestFixture.defaultInstanceListControl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InstanceListControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PInstanceListControl control = defaultInstanceListControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        App app = appRepository.byId(response.appId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertTrue(updatedControl.isComplete());
    }

    @Test
    public void should_fetch_instance_list_presentation_value() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PInstanceListControl control = defaultInstanceListControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        CreateQrResponse response1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse response2 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        QR qr1 = qrRepository.byId(response1.getQrId());
        QR qr2 = qrRepository.byId(response2.getQrId());

        QInstanceListPresentation presentation = (QInstanceListPresentation) PresentationApi.fetchPresentation(response.jwt(), response1.getQrId(), response.homePageId(), control.getId());

        assertEquals(qr2.getPlateId(), presentation.getInstances().get(0).getPlateId());
        assertEquals(qr2.getName(), presentation.getInstances().get(0).getName());
        assertEquals(qr2.getCreator(), presentation.getInstances().get(0).getCreator());
        assertEquals(qr2.getCreatedAt(), presentation.getInstances().get(0).getCreatedAt());

        assertEquals(qr1.getPlateId(), presentation.getInstances().get(1).getPlateId());
    }

}
