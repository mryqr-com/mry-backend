package com.mryqr.core.qr;

import com.mryqr.BaseApiTest;
import com.mryqr.common.utils.PagedList;
import com.mryqr.core.qr.command.*;
import com.mryqr.core.qr.command.importqr.QrImportResponse;
import com.mryqr.core.qr.query.QQrBaseSetting;
import com.mryqr.core.qr.query.QQrSummary;
import com.mryqr.core.qr.query.bindplate.QBindPlateInfo;
import com.mryqr.core.qr.query.list.ListViewableQrsQuery;
import com.mryqr.core.qr.query.list.QViewableListQr;
import com.mryqr.core.qr.query.plate.ListPlateAttributeValuesQuery;
import com.mryqr.core.qr.query.submission.QSubmissionQr;
import com.mryqr.core.qr.query.submission.list.ListQrSubmissionsQuery;
import com.mryqr.core.submission.query.list.QListSubmission;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.io.File;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;
import static com.mryqr.utils.RandomTestFixture.rQrName;

public class QrApi {

    public static Response createQrRaw(String jwt, CreateQrCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/qrs");
    }

    public static CreateQrResponse createQr(String jwt, CreateQrCommand command) {
        return createQrRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static CreateQrResponse createQr(String jwt, String name, String groupId) {
        return createQr(jwt, CreateQrCommand.builder().name(name).groupId(groupId).build());
    }

    public static CreateQrResponse createQr(String jwt, String groupId) {
        return createQr(jwt, CreateQrCommand.builder().name(rQrName()).groupId(groupId).build());
    }

    public static Response importQrExcelRaw(String jwt, String groupId, File file) {
        return BaseApiTest.given(jwt)
                .contentType("multipart/form-data")
                .multiPart("groupId", groupId)
                .multiPart("file", file)
                .when()
                .post("/qrs/import");
    }

    public static QrImportResponse importQrExcel(String jwt, String groupId, File file) {
        return importQrExcelRaw(jwt, groupId, file)
                .then()
                .statusCode(200)
                .extract()
                .as(QrImportResponse.class);
    }

    public static Response createQrFromPlateRaw(String jwt, CreateQrFromPlateCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/qrs/from-plate");
    }

    public static CreateQrResponse createQrFromPlate(String jwt, CreateQrFromPlateCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/qrs/from-plate")
                .then()
                .statusCode(201)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static CreateQrResponse createQrFromPlate(String jwt, String name, String groupId, String plateId) {
        CreateQrFromPlateCommand command = CreateQrFromPlateCommand.builder().name(name).groupId(groupId).plateId(plateId).build();
        return createQrFromPlate(jwt, command);
    }

    public static Response renameQrRaw(String jwt, String qrId, RenameQrCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/qrs/{id}/name", qrId);
    }

    public static void renameQr(String jwt, String qrId, RenameQrCommand command) {
        renameQrRaw(jwt, qrId, command).then().statusCode(200);
    }

    public static void renameQr(String jwt, String qrId, String qrName) {
        renameQr(jwt, qrId, RenameQrCommand.builder().name(qrName).build());
    }

    public static Response changeQrsGroupRaw(String jwt, ChangeQrsGroupCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/qrs/group");
    }

    public static void changeQrsGroup(String jwt, ChangeQrsGroupCommand command) {
        changeQrsGroupRaw(jwt, command).then().statusCode(200);
    }

    public static void changeQrsGroup(String jwt, String groupId, String... qrIds) {
        changeQrsGroup(jwt, ChangeQrsGroupCommand.builder().groupId(groupId).qrIds(newHashSet(qrIds)).build());
    }

    public static QSubmissionQr fetchSubmissionQr(String jwt, String plateId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/qrs/submission-qrs/{plateId}", plateId)
                .then()
                .statusCode(200)
                .extract()
                .as(QSubmissionQr.class);
    }

    public static QViewableListQr fetchListedQr(String jwt, String qrId) {
        return fetchListedQrRaw(jwt, qrId)
                .then()
                .statusCode(200)
                .extract()
                .as(QViewableListQr.class);
    }

    public static Response fetchListedQrRaw(String jwt, String qrId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/qrs/my-viewable-qrs/{qrId}", qrId);
    }

    public static Response updateQrBaseSettingRaw(String jwt, String qrId, UpdateQrBaseSettingCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/qrs/{id}/base-setting", qrId);
    }

    public static void updateQrBaseSetting(String jwt, String qrId, UpdateQrBaseSettingCommand command) {
        updateQrBaseSettingRaw(jwt, qrId, command)
                .then()
                .statusCode(200);
    }

    public static Response listQrsRaw(String jwt, ListViewableQrsQuery command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/qrs/my-viewable-qrs");
    }

    public static PagedList<QViewableListQr> listQrs(String jwt, ListViewableQrsQuery command) {
        return listQrsRaw(jwt, command)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static byte[] exportQrsAsExcel(String jwt, ListViewableQrsQuery command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/qrs/export")
                .then()
                .extract()
                .asByteArray();
    }

    public static QQrBaseSetting fetchQrBaseSetting(String jwt, String qrId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/qrs/{id}/base-setting", qrId)
                .then()
                .statusCode(200)
                .extract()
                .as(QQrBaseSetting.class);
    }

    public static QQrSummary fetchQrSummary(String jwt, String qrId) {
        return fetchQrSummaryRaw(jwt, qrId)
                .then()
                .statusCode(200)
                .extract()
                .as(QQrSummary.class);
    }

    public static Response fetchQrSummaryRaw(String jwt, String qrId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/qrs/{id}/summary", qrId);
    }

    public static Response resetPlateRaw(String jwt, String qrId, ResetQrPlateCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/qrs/{id}/plate", qrId);
    }

    public static void resetPlate(String jwt, String qrId, ResetQrPlateCommand command) {
        resetPlateRaw(jwt, qrId, command).then().statusCode(200);
    }


    public static void resetPlate(String jwt, String qrId, String plateId) {
        resetPlateRaw(jwt, qrId, ResetQrPlateCommand.builder().plateId(plateId).build()).then().statusCode(200);
    }

    public static Response resetCirculationStatusRaw(String jwt, String qrId, String optionId) {
        return BaseApiTest.given(jwt)
                .body(ResetQrCirculationStatusCommand.builder().circulationOptionId(optionId).build())
                .when()
                .put("/qrs/{id}/circulation-status", qrId);
    }

    public static void resetCirculationStatus(String jwt, String qrId, String optionId) {
        resetCirculationStatusRaw(jwt, qrId, optionId).then().statusCode(200);
    }

    public static Response deleteQrsRaw(String jwt, DeleteQrsCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/qrs/deletion");
    }

    public static void deleteQrs(String jwt, DeleteQrsCommand command) {
        deleteQrsRaw(jwt, command).then().statusCode(200);
    }

    public static void deleteQrs(String jwt, String... qrIds) {
        deleteQrs(jwt, DeleteQrsCommand.builder().qrIds(newHashSet(qrIds)).build());
    }

    public static Response deleteQrRaw(String jwt, String qrId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/qrs/{id}", qrId);
    }

    public static void deleteQr(String jwt, String qrId) {
        deleteQrRaw(jwt, qrId).then().statusCode(200);
    }

    public static void markTemplate(String jwt, String qrId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/qrs/{qrId}/template", qrId)
                .then()
                .statusCode(200);
    }

    public static Response unmarkTemplateRaw(String jwt, String qrId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/qrs/{qrId}/template", qrId);
    }

    public static void unmarkTemplate(String jwt, String qrId) {
        unmarkTemplateRaw(jwt, qrId)
                .then()
                .statusCode(200);
    }

    public static void activate(String jwt, String qrId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/qrs/{qrId}/activation", qrId)
                .then()
                .statusCode(200);
    }

    public static void deactivate(String jwt, String qrId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/qrs/{qrId}/deactivation", qrId)
                .then()
                .statusCode(200);
    }

    public static Response listQrSubmissionsRaw(String jwt, String qrId, ListQrSubmissionsQuery command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/qrs/{qrId}/submissions", qrId);
    }

    public static PagedList<QListSubmission> listQrSubmissions(String jwt, String qrId, ListQrSubmissionsQuery command) {
        return listQrSubmissionsRaw(jwt, qrId, command)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static Response fetchBindPlateInfoRaw(String jwt, String plateId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/qrs/bind-plate-infos/{plateId}", plateId);
    }

    public static QBindPlateInfo fetchBindPlateInfo(String jwt, String plateId) {
        return fetchBindPlateInfoRaw(jwt, plateId)
                .then()
                .statusCode(200)
                .extract()
                .as(QBindPlateInfo.class);
    }

    public static Response listPlateAttributeValuesRaw(String jwt, ListPlateAttributeValuesQuery command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/qrs/plate-attribute-values");
    }

    public static Map<String, Map<String, String>> listPlateAttributeValues(String jwt, ListPlateAttributeValuesQuery command) {
        return listPlateAttributeValuesRaw(jwt, command)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

}
