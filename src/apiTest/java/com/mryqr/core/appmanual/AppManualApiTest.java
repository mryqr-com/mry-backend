package com.mryqr.core.appmanual;

import com.mryqr.BaseApiTest;
import com.mryqr.core.appmanual.command.UpdateAppManualCommand;
import com.mryqr.core.appmanual.query.QAppManual;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;

import static com.mryqr.utils.RandomTestFixture.rSentence;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.commons.util.StringUtils.isBlank;

public class AppManualApiTest extends BaseApiTest {

    @Test
    public void should_fetch_empty_manual_if_no_data() {
        PreparedAppResponse response = setupApi.registerWithApp();

        assertTrue(isBlank(BaseApiTest.given(response.getJwt())
                .when()
                .get("/app-manuals/{appId}", response.getAppId())
                .then()
                .statusCode(200).extract().body().asString()));
    }

    @Test
    public void should_update_app_manual() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String content = rSentence(10);
        AppManualApi.updateAppManual(response.getJwt(), response.getAppId(), UpdateAppManualCommand.builder().content(content).build());

        QAppManual qAppManual = AppManualApi.fetchAppManual(response.getJwt(), response.getAppId());
        assertEquals(response.getAppId(), qAppManual.getAppId());
        assertEquals(content, qAppManual.getContent());
        assertNotNull(qAppManual.getId());

        String updatedContent = rSentence(10);
        AppManualApi.updateAppManual(response.getJwt(), response.getAppId(), UpdateAppManualCommand.builder().content(updatedContent).build());
        QAppManual updatedQAppManual = AppManualApi.fetchAppManual(response.getJwt(), response.getAppId());
        assertEquals(qAppManual.getId(), updatedQAppManual.getId());
        assertEquals(updatedContent, updatedQAppManual.getContent());
    }
}
