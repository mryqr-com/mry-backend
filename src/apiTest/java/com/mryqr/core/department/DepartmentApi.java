package com.mryqr.core.department;

import com.mryqr.BaseApiTest;
import com.mryqr.common.utils.ReturnId;
import com.mryqr.core.department.command.CreateDepartmentCommand;
import com.mryqr.core.department.command.RenameDepartmentCommand;
import io.restassured.response.Response;

public class DepartmentApi {
    public static Response createDepartmentRaw(String jwt, CreateDepartmentCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/departments");
    }

    public static String createDepartment(String jwt, CreateDepartmentCommand command) {
        return createDepartmentRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class)
                .getId();
    }

    public static String createDepartment(String jwt, String name) {
        return createDepartmentRaw(jwt, CreateDepartmentCommand.builder().name(name).build())
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class)
                .getId();
    }

    public static Response renameDepartmentRaw(String jwt, String departmentId, RenameDepartmentCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/departments/{departmentId}/name", departmentId);

    }

    public static String createDepartmentWithParent(String jwt, String parentDepartmentId, String name) {
        return createDepartment(jwt, CreateDepartmentCommand.builder().name(name).parentDepartmentId(parentDepartmentId).build());
    }

    public static void renameDepartment(String jwt, String departmentId, RenameDepartmentCommand command) {
        renameDepartmentRaw(jwt, departmentId, command)
                .then()
                .statusCode(200);
    }

    public static Response addDepartmentManagerRaw(String jwt, String departmentId, String memberId) {
        return BaseApiTest.given(jwt)
                .when()
                .put("/departments/{departmentId}/managers/{memberId}", departmentId, memberId);
    }

    public static void addDepartmentManager(String jwt, String departmentId, String memberId) {
        addDepartmentManagerRaw(jwt, departmentId, memberId).then().statusCode(200);
    }

    public static Response removeDepartmentManagerRaw(String jwt, String departmentId, String memberId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/departments/{departmentId}/managers/{memberId}", departmentId, memberId);
    }

    public static void removeDepartmentManager(String jwt, String departmentId, String memberId) {
        removeDepartmentManagerRaw(jwt, departmentId, memberId).then().statusCode(200);
    }

    public static Response deleteDepartmentRaw(String jwt, String departmentId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/departments/{departmentId}", departmentId);
    }

    public static void deleteDepartment(String jwt, String departmentId) {
        deleteDepartmentRaw(jwt, departmentId)
                .then()
                .statusCode(200);
    }

}
