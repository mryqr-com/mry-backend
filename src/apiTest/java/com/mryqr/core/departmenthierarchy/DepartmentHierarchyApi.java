package com.mryqr.core.departmenthierarchy;

import com.mryqr.BaseApiTest;
import com.mryqr.core.departmenthierarchy.command.UpdateDepartmentHierarchyCommand;
import com.mryqr.core.departmenthierarchy.query.QDepartmentHierarchy;
import io.restassured.response.Response;

public class DepartmentHierarchyApi {
    public static QDepartmentHierarchy fetchDepartmentHierarchy(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/department-hierarchy")
                .then()
                .statusCode(200)
                .extract()
                .as(QDepartmentHierarchy.class);
    }

    public static Response updateDepartmentHierarchyRaw(String jwt, UpdateDepartmentHierarchyCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/department-hierarchy");
    }

    public static void updateDepartmentHierarchy(String jwt, UpdateDepartmentHierarchyCommand command) {
        updateDepartmentHierarchyRaw(jwt, command).then().statusCode(200);
    }

}
