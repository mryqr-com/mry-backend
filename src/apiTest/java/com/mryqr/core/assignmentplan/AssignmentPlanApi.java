package com.mryqr.core.assignmentplan;

import com.mryqr.BaseApiTest;
import com.mryqr.core.assignmentplan.command.CreateAssignmentPlanCommand;
import com.mryqr.core.assignmentplan.command.ExcludeGroupsCommand;
import com.mryqr.core.assignmentplan.command.SetGroupOperatorsCommand;
import com.mryqr.core.assignmentplan.command.UpdateAssignmentPlanSettingCommand;
import com.mryqr.core.assignmentplan.query.QAssignmentPlan;
import com.mryqr.core.assignmentplan.query.QAssignmentPlanSummary;
import com.mryqr.core.common.utils.ReturnId;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.util.List;

public class AssignmentPlanApi {
    public static Response createAssignmentPlanRaw(String jwt, CreateAssignmentPlanCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/assignment-plans");
    }

    public static String createAssignmentPlan(String jwt, CreateAssignmentPlanCommand command) {
        return createAssignmentPlanRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static Response updateAssignmentPlanSettingRaw(String jwt, String assignmentPlanId, UpdateAssignmentPlanSettingCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/assignment-plans/{id}/setting", assignmentPlanId);
    }

    public static void updateAssignmentPlanSetting(String jwt, String assignmentPlanId, UpdateAssignmentPlanSettingCommand command) {
        updateAssignmentPlanSettingRaw(jwt, assignmentPlanId, command)
                .then()
                .statusCode(200);
    }

    public static Response excludeGroupsRaw(String jwt, String assignmentPlanId, ExcludeGroupsCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/assignment-plans/{id}/excluded-groups", assignmentPlanId);
    }

    public static void excludeGroups(String jwt, String assignmentPlanId, ExcludeGroupsCommand command) {
        excludeGroupsRaw(jwt, assignmentPlanId, command)
                .then()
                .statusCode(200);
    }

    public static Response setGroupOperatorsRaw(String jwt, String assignmentPlanId, SetGroupOperatorsCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/assignment-plans/{id}/group-operators", assignmentPlanId);
    }

    public static void setGroupOperators(String jwt, String assignmentPlanId, SetGroupOperatorsCommand command) {
        setGroupOperatorsRaw(jwt, assignmentPlanId, command)
                .then()
                .statusCode(200);
    }

    public static void activateAssignmentPlan(String jwt, String assignmentPlanId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/assignment-plans/{id}/activation", assignmentPlanId)
                .then()
                .statusCode(200);
    }

    public static void deactivateAssignmentPlan(String jwt, String assignmentPlanId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/assignment-plans/{id}/deactivation", assignmentPlanId)
                .then()
                .statusCode(200);
    }

    public static List<QAssignmentPlan> listAssignmentPlans(String jwt, String appId, String groupId) {
        return BaseApiTest.given(jwt)
                .when()
                .param("groupId", groupId)
                .get("/assignment-plans/apps/{appId}", appId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static List<QAssignmentPlan> listAssignmentPlans(String jwt, String appId) {
        return listAssignmentPlans(jwt, appId, null);
    }

    public static void deleteAssignmentPlan(String jwt, String assignmentPlanId) {
        BaseApiTest.given(jwt)
                .when()
                .delete("/assignment-plans/{id}", assignmentPlanId)
                .then()
                .statusCode(200);
    }

    public static List<QAssignmentPlanSummary> listAssignmentPlanSummaries(String jwt, String appId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/assignment-plans/apps/{appId}/summaries", appId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static List<QAssignmentPlanSummary> listAssignmentPlanSummariesForGroup(String jwt, String groupId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/assignment-plans/groups/{groupId}/summaries", groupId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

}
