package com.mryqr.core.platebatch;

import com.mryqr.BaseApiTest;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.ReturnId;
import com.mryqr.core.platebatch.command.CreatePlateBatchCommand;
import com.mryqr.core.platebatch.command.RenamePlateBatchCommand;
import com.mryqr.core.platebatch.query.ListMyManagedPlateBatchesQuery;
import com.mryqr.core.platebatch.query.QManagedListPlateBatch;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.util.List;

import static com.mryqr.utils.RandomTestFixture.rPlateBatchName;

public class PlateBatchApi {
    public static Response createPlateBatchRaw(String jwt, CreatePlateBatchCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/platebatches");
    }

    public static String createPlateBatch(String jwt, CreatePlateBatchCommand command) {
        return createPlateBatchRaw(jwt, command)
                .then().statusCode(201)
                .extract()
                .as(ReturnId.class).getId();
    }

    public static String createPlateBatch(String jwt, String appId, int total) {
        CreatePlateBatchCommand command = CreatePlateBatchCommand.builder().appId(appId).name(rPlateBatchName()).total(total).build();
        return createPlateBatch(jwt, command);
    }

    public static Response renamePlateBatchRaw(String jwt, String plateBatchId, RenamePlateBatchCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/platebatches/{id}/name", plateBatchId);
    }

    public static void renamePlateBatch(String jwt, String plateBatchId, RenamePlateBatchCommand command) {
        renamePlateBatchRaw(jwt, plateBatchId, command).then().statusCode(200);
    }

    public static void renamePlateBatch(String jwt, String plateBatchId, String newName) {
        renamePlateBatch(jwt, plateBatchId, RenamePlateBatchCommand.builder().name(newName).build());
    }

    public static void deletePlateBatch(String jwt, String plateBatchId) {
        BaseApiTest.given(jwt)
                .when()
                .delete("/platebatches/{id}", plateBatchId)
                .then()
                .statusCode(200);
    }

    public static PagedList<QManagedListPlateBatch> listPlateBatches(String jwt, ListMyManagedPlateBatchesQuery query) {
        return BaseApiTest.given(jwt)
                .body(query)
                .when()
                .post("/platebatches/my-managed-platebatches")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static List<String> allPlateIdsUnderPlateBatch(String jwt, String batchId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/platebatches/{batchId}/plate-ids", batchId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static List<String> unusedPlateIdsUnderPlateBatch(String jwt, String batchId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/platebatches/{batchId}/unused-plate-ids", batchId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }


}
