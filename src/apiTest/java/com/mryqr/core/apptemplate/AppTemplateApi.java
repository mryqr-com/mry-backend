package com.mryqr.core.apptemplate;

import com.mryqr.BaseApiTest;
import com.mryqr.core.apptemplate.query.ListAppTemplateQuery;
import com.mryqr.core.apptemplate.query.QDetailedAppTemplate;
import com.mryqr.core.apptemplate.query.QListAppTemplate;
import com.mryqr.core.common.utils.PagedList;
import io.restassured.common.mapper.TypeRef;

public class AppTemplateApi {

    public static PagedList<QListAppTemplate> listPublishedAppTemplates(ListAppTemplateQuery query) {
        return BaseApiTest.given()
                .body(query)
                .when()
                .post("/apptemplates/published-lists")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static QDetailedAppTemplate fetchAppTemplateDetail(String appTemplateId) {
        return BaseApiTest.given()
                .when()
                .get("/apptemplates/{appTemplateId}", appTemplateId)
                .then()
                .statusCode(200)
                .extract()
                .as(QDetailedAppTemplate.class);
    }

}
