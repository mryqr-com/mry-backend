package com.mryqr.core.plan;

import com.mryqr.BaseApiTest;
import com.mryqr.core.plan.query.QListPlan;
import io.restassured.common.mapper.TypeRef;

import java.util.List;

public class PlanApi {
    public static List<QListPlan> listPlans() {
        return BaseApiTest.given()
                .when()
                .get("/plans")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }
}
