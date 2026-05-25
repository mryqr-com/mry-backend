package com.mryqr.core.grouphierarchy;

import com.mryqr.BaseApiTest;
import com.mryqr.core.grouphierarchy.command.UpdateGroupHierarchyCommand;
import com.mryqr.core.grouphierarchy.query.QGroupHierarchy;
import io.restassured.response.Response;

public class GroupHierarchyApi {
    public static QGroupHierarchy fetchGroupHierarchy(String jwt, String appId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/group-hierarchies/apps/{appId}", appId)
                .then()
                .statusCode(200)
                .extract()
                .as(QGroupHierarchy.class);
    }

    public static Response updateGroupHierarchyRaw(String jwt, String appId, UpdateGroupHierarchyCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/group-hierarchies/apps/{appId}", appId);
    }

    public static void updateGroupHierarchy(String jwt, String appId, UpdateGroupHierarchyCommand command) {
        updateGroupHierarchyRaw(jwt, appId, command).then().statusCode(200);
    }

}
