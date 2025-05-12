package com.mryqr.core.platetemplate;

import com.mryqr.BaseApiTest;
import com.mryqr.core.common.utils.ReturnId;
import com.mryqr.core.platetemplate.command.CreatePlateTemplateCommand;
import com.mryqr.core.platetemplate.command.UpdatePlateTemplateCommand;
import com.mryqr.core.platetemplate.query.QListPlateTemplate;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.util.List;

public class PlateTemplateApi {
    public static Response createPlateTemplateRaw(String jwt, CreatePlateTemplateCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/plate-templates");
    }

    public static String createPlateTemplate(String jwt, CreatePlateTemplateCommand command) {
        return createPlateTemplateRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class)
                .getId();
    }

    public static void updatePlateTemplate(String jwt, String templateId, UpdatePlateTemplateCommand command) {
        BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/plate-templates/{id}", templateId)
                .then()
                .statusCode(200);
    }

    public static List<QListPlateTemplate> listPlateTemplates(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/plate-templates")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static void deletePlateTemplate(String jwt, String templateId) {
        BaseApiTest.given(jwt)
                .when()
                .delete("/plate-templates/{templateId}", templateId)
                .then()
                .statusCode(200);
    }


}
