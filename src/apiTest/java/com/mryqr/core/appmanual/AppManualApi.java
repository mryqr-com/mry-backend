package com.mryqr.core.appmanual;

import com.mryqr.BaseApiTest;
import com.mryqr.core.appmanual.command.UpdateAppManualCommand;
import com.mryqr.core.appmanual.query.QAppManual;
import io.restassured.common.mapper.TypeRef;

public class AppManualApi {
    public static void updateAppManual(String jwt, String appId, UpdateAppManualCommand command) {
        BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/app-manuals/{appId}", appId)
                .then()
                .statusCode(200);
    }

    public static QAppManual fetchAppManual(String jwt, String appId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/app-manuals/{appId}", appId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

}
