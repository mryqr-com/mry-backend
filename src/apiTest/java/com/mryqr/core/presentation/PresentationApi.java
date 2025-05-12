package com.mryqr.core.presentation;

import com.mryqr.BaseApiTest;
import com.mryqr.core.presentation.query.QControlPresentation;
import io.restassured.response.Response;

public class PresentationApi {

    public static QControlPresentation fetchPresentation(String jwt, String qrId, String pageId, String controlId) {
        return fetchPresentationRaw(jwt, qrId, pageId, controlId)
                .then()
                .statusCode(200)
                .extract()
                .as(QControlPresentation.class);
    }

    public static Response fetchPresentationRaw(String jwt, String qrId, String pageId, String controlId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/presentations/{qrId}/{pageId}/{controlId}", qrId, pageId, controlId);
    }

}
