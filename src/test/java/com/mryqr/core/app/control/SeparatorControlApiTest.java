package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PSeparatorControl;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.RandomTestFixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeparatorControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PSeparatorControl control = RandomTestFixture.defaultSeparatorControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);

        App app = appRepository.byId(response.appId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

}
