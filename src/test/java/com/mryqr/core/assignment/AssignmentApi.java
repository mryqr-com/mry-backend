package com.mryqr.core.assignment;

import com.mryqr.BaseApiTest;
import com.mryqr.common.utils.PagedList;
import com.mryqr.core.assignment.command.SetAssignmentOperatorsCommand;
import com.mryqr.core.assignment.query.*;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

public class AssignmentApi {

    public static void deleteAssignment(String jwt, String assignmentId) {
        BaseApiTest.given(jwt)
                .when()
                .delete("/assignments/{id}", assignmentId)
                .then()
                .statusCode(200);
    }

    public static Response setOperatorsRaw(String jwt, String assignmentId, SetAssignmentOperatorsCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/assignments/{id}/operators", assignmentId);
    }

    public static void setOperators(String jwt, String assignmentId, SetAssignmentOperatorsCommand command) {
        setOperatorsRaw(jwt, assignmentId, command)
                .then()
                .statusCode(200);
    }

    public static Response listManagedAssignmentsRaw(String jwt, ListMyManagedAssignmentsQuery command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/assignments/my-managed-assignments");
    }

    public static PagedList<QListAssignment> listManagedAssignments(String jwt, ListMyManagedAssignmentsQuery command) {
        return listManagedAssignmentsRaw(jwt, command)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static Response listMyAssignmentsRaw(String jwt, ListMyAssignmentsQuery command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/assignments/my-assignments");
    }

    public static PagedList<QListAssignment> listMyAssignments(String jwt, ListMyAssignmentsQuery command) {
        return listMyAssignmentsRaw(jwt, command)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static Response listAssignmentQrsRaw(String jwt,
                                                String assignmentId,
                                                ListAssignmentQrsQuery command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/assignments/{id}/qrs", assignmentId);
    }

    public static PagedList<QAssignmentListQr> listAssignmentQrs(String jwt,
                                                                 String assignmentId,
                                                                 ListAssignmentQrsQuery command) {
        return listAssignmentQrsRaw(jwt, assignmentId, command)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static Response fetchAssignmentDetailRaw(String jwt, String assignmentId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/assignments/{assignmentId}/detail", assignmentId);
    }

    public static QAssignmentDetail fetchAssignmentDetail(String jwt, String assignmentId) {
        return fetchAssignmentDetailRaw(jwt, assignmentId)
                .then()
                .statusCode(200)
                .extract()
                .as(QAssignmentDetail.class);
    }

    public static Response fetchAssignmentQrDetailRaw(String jwt, String assignmentId, String qrId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/assignments/{assignmentId}/qrs/{qrId}/detail", assignmentId, qrId);
    }

    public static QAssignmentQrDetail fetchAssignmentQrDetail(String jwt, String assignmentId, String qrId) {
        return fetchAssignmentQrDetailRaw(jwt, assignmentId, qrId)
                .then()
                .statusCode(200)
                .extract()
                .as(QAssignmentQrDetail.class);
    }
}
