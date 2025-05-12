package com.mryqr.integration;

import com.mryqr.BaseApiTest;
import com.mryqr.common.utils.ReturnId;
import com.mryqr.integration.app.query.QIntegrationApp;
import com.mryqr.integration.app.query.QIntegrationListApp;
import com.mryqr.integration.department.command.IntegrationCreateDepartmentCommand;
import com.mryqr.integration.department.command.IntegrationUpdateDepartmentCustomIdCommand;
import com.mryqr.integration.department.query.QIntegrationDepartment;
import com.mryqr.integration.department.query.QIntegrationListDepartment;
import com.mryqr.integration.group.command.*;
import com.mryqr.integration.group.query.QIntegrationGroup;
import com.mryqr.integration.group.query.QIntegrationListGroup;
import com.mryqr.integration.member.command.IntegrationCreateMemberCommand;
import com.mryqr.integration.member.command.IntegrationUpdateMemberCustomIdCommand;
import com.mryqr.integration.member.command.IntegrationUpdateMemberInfoCommand;
import com.mryqr.integration.member.query.QIntegrationListMember;
import com.mryqr.integration.member.query.QIntegrationMember;
import com.mryqr.integration.qr.command.*;
import com.mryqr.integration.qr.query.QIntegrationQr;
import com.mryqr.integration.submission.command.IntegrationNewSubmissionCommand;
import com.mryqr.integration.submission.command.IntegrationUpdateSubmissionCommand;
import com.mryqr.integration.submission.query.QIntegrationSubmission;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.util.List;

public class IntegrationApi {
    public static Response listAppsRaw(String username, String password) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/apps");
    }

    public static List<QIntegrationListApp> listApps(String username, String password) {
        return listAppsRaw(username, password)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static QIntegrationApp fetchApp(String username, String password, String appId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/apps/{id}", appId)
                .then()
                .statusCode(200)
                .extract()
                .as(QIntegrationApp.class);
    }

    public static void activateApp(String username, String password, String appId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/apps/{appId}/activation", appId)
                .then()
                .statusCode(200);
    }

    public static void deactivateApp(String username, String password, String appId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/apps/{appId}/deactivation", appId)
                .then()
                .statusCode(200);
    }

    public static String createSubmission(String username, String password, String qrId, IntegrationNewSubmissionCommand command) {
        return createSubmissionRaw(username, password, qrId, command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static Response createSubmissionRaw(String username, String password, String qrId, IntegrationNewSubmissionCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .post("/integration/qrs/{qrId}/submissions", qrId);
    }

    public static String createSubmissionByQrCustomId(String username, String password, String appId, String qrCustomId, IntegrationNewSubmissionCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .post("/integration/apps/{appId}/qrs/custom/{customId}/submissions", appId, qrCustomId)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static QIntegrationSubmission fetchSubmission(String username, String password, String submissionId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/submissions/{id}", submissionId)
                .then()
                .statusCode(200)
                .extract()
                .as(QIntegrationSubmission.class);
    }

    public static void updateSubmission(String username, String password, String submissionId, IntegrationUpdateSubmissionCommand command) {
        updateSubmissionRaw(username, password, submissionId, command)
                .then()
                .statusCode(200);
    }

    public static Response updateSubmissionRaw(String username, String password, String submissionId, IntegrationUpdateSubmissionCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/submissions/{submissionId}", submissionId);
    }

    public static void deleteSubmission(String username, String password, String submissionId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .delete("/integration/submissions/{submissionId}", submissionId)
                .then()
                .statusCode(200);
    }

    public static IntegrationCreateQrResponse createQrSimple(String username, String password, IntegrationCreateQrSimpleCommand command) {
        return createQrSimpleRaw(username, password, command)
                .then()
                .statusCode(201)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static Response createQrSimpleRaw(String username, String password, IntegrationCreateQrSimpleCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .post("/integration/qrs/simple-creation");
    }

    public static IntegrationCreateQrResponse createQrAdvanced(String username, String password, IntegrationCreateQrAdvancedCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .post("/integration/qrs/advanced-creation")
                .then()
                .statusCode(201)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static void deleteQr(String username, String password, String qrId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .delete("/integration/qrs/{id}", qrId).then().statusCode(200);
    }

    public static void deleteQrByCustomId(String username, String password, String appId, String customId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .delete("/integration/apps/{appId}/qrs/custom/{customId}", appId, customId).then().statusCode(200);
    }

    public static void activateQr(String username, String password, String qrId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/qrs/{id}/activation", qrId)
                .then().statusCode(200);
    }

    public static void deactivateQr(String username, String password, String qrId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/qrs/{id}/deactivation", qrId)
                .then().statusCode(200);
    }

    public static void activateQrByCustomId(String username, String password, String appId, String customId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/apps/{appId}/qrs/custom/{customId}/activation", appId, customId)
                .then().statusCode(200);
    }

    public static void deactivateQrByCustomId(String username, String password, String appId, String customId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/apps/{appId}/qrs/custom/{customId}/deactivation", appId, customId)
                .then().statusCode(200);
    }

    public static void renameQr(String username, String password, String qrId, IntegrationRenameQrCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/qrs/{id}/name", qrId).then().statusCode(200);
    }

    public static void renameQrByCustomId(String username, String password, String appId, String customId, IntegrationRenameQrCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/apps/{appId}/qrs/custom/{customId}/name", appId, customId).then().statusCode(200);
    }

    public static void updateQrBaseSetting(String username, String password, String qrId, IntegrationUpdateQrBaseSettingCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/qrs/{qrId}/base-setting", qrId)
                .then()
                .statusCode(200);
    }

    public static void updateQrBaseSettingByCustomId(String username, String password, String appId, String customId, IntegrationUpdateQrBaseSettingCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/apps/{appId}/qrs/custom/{customId}/base-setting", appId, customId)
                .then()
                .statusCode(200);
    }

    public static void updateQrDescription(String username, String password, String qrId, IntegrationUpdateQrDescriptionCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/qrs/{id}/description", qrId).then().statusCode(200);
    }

    public static void updateQrDescriptionByCustomId(String username, String password, String appId, String customId, IntegrationUpdateQrDescriptionCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/apps/{appId}/qrs/custom/{customId}/description", appId, customId).then().statusCode(200);
    }

    public static void updateQrHeaderImage(String username, String password, String qrId, IntegrationUpdateQrHeaderImageCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/qrs/{id}/header-image", qrId).then().statusCode(200);
    }

    public static void updateQrHeaderImageByCustomId(String username, String password, String appId, String customId, IntegrationUpdateQrHeaderImageCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/apps/{appId}/qrs/custom/{customId}/header-image", appId, customId).then().statusCode(200);
    }

    public static void updateQrDirectAttributes(String username, String password, String qrId, IntegrationUpdateQrDirectAttributesCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/qrs/{id}/direct-attribute-values", qrId).then().statusCode(200);
    }

    public static void updateQrDirectAttributesByCustomId(String username, String password, String appId, String customId, IntegrationUpdateQrDirectAttributesCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/apps/{appId}/qrs/custom/{customId}/direct-attribute-values", appId, customId).then().statusCode(200);
    }

    public static void updateQrGeolocation(String username, String password, String qrId, IntegrationUpdateQrGeolocationCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/qrs/{id}/geolocation", qrId).then().statusCode(200);
    }

    public static void updateQrGeolocationByCustomId(String username, String password, String appId, String customId, IntegrationUpdateQrGeolocationCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/apps/{appId}/qrs/custom/{customId}/geolocation", appId, customId).then().statusCode(200);
    }

    public static void updateQrCustomId(String username, String password, String qrId, IntegrationUpdateQrCustomIdCommand command) {
        updateQrCustomIdRaw(username, password, qrId, command)
                .then()
                .statusCode(200);
    }

    public static Response updateQrCustomIdRaw(String username, String password, String qrId, IntegrationUpdateQrCustomIdCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/qrs/{qrId}/custom-id", qrId);
    }

    public static QIntegrationQr fetchQr(String username, String password, String qrId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/qrs/{id}", qrId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static QIntegrationQr fetchQrByCustomId(String username, String password, String appId, String customId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/apps/{appId}/qrs/custom/{customId}", appId, customId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static String createGroup(String username, String password, IntegrationCreateGroupCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .post("/integration/groups")
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static void updateGroupCustomId(String username, String password, String groupId, IntegrationUpdateGroupCustomIdCommand command) {
        updateGroupCustomIdRaw(username, password, groupId, command)
                .then()
                .statusCode(200);
    }

    public static Response updateGroupCustomIdRaw(String username, String password, String groupId, IntegrationUpdateGroupCustomIdCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/groups/{groupId}/custom-id", groupId);
    }

    public static void deleteGroup(String username, String password, String groupId) {
        deleteGroupRaw(username, password, groupId).then()
                .statusCode(200);
    }

    public static Response deleteGroupRaw(String username, String password, String groupId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .delete("/integration/groups/{id}", groupId);
    }

    public static void deleteGroupByCustomId(String username, String password, String appId, String customId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .delete("/integration/apps/{appId}/groups/custom/{customId}", appId, customId).then().statusCode(200);
    }

    public static void renameGroup(String username, String password, String qrId, IntegrationRenameGroupCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/groups/{id}/name", qrId).then().statusCode(200);
    }

    public static void renameGroupByCustomId(String username, String password, String appId, String customId, IntegrationRenameGroupCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/apps/{appId}/groups/custom/{customId}/name", appId, customId).then().statusCode(200);
    }

    public static void archiveGroup(String username, String password, String groupId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/groups/{id}/archive", groupId)
                .then()
                .statusCode(200);
    }

    public static void archiveGroupByCustomId(String username, String password, String appId, String customId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/apps/{appId}/groups/custom/{customId}/archive", appId, customId)
                .then()
                .statusCode(200);
    }

    public static void unArchiveGroup(String username, String password, String groupId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/groups/{id}/unarchive", groupId)
                .then()
                .statusCode(200);
    }

    public static void unArchiveGroupByCustomId(String username, String password, String appId, String customId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/apps/{appId}/groups/custom/{customId}/unarchive", appId, customId)
                .then()
                .statusCode(200);
    }

    public static void activateGroup(String username, String password, String groupId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/groups/{id}/activation", groupId)
                .then()
                .statusCode(200);
    }

    public static void activationGroupByCustomId(String username, String password, String appId, String customId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/apps/{appId}/groups/custom/{customId}/activation", appId, customId)
                .then()
                .statusCode(200);
    }

    public static void deactivateGroup(String username, String password, String groupId) {
        deactivateGroupRaw(username, password, groupId)
                .then()
                .statusCode(200);
    }

    public static Response deactivateGroupRaw(String username, String password, String groupId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/groups/{id}/deactivation", groupId);
    }

    public static void deactivationGroupByCustomId(String username, String password, String appId, String customId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/apps/{appId}/groups/custom/{customId}/deactivation", appId, customId)
                .then()
                .statusCode(200);
    }

    public static void addGroupManagers(String username, String password, String groupId, IntegrationAddGroupManagersCommand command) {
        addGroupManagersRaw(username, password, groupId, command)
                .then()
                .statusCode(200);
    }

    public static Response addGroupManagersRaw(String username, String password, String groupId, IntegrationAddGroupManagersCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/groups/{groupId}/managers/additions", groupId);
    }

    public static void addGroupManagersByCustomId(String username, String password, String appId, String customId, IntegrationCustomAddGroupManagersCommand command) {
        addGroupManagersByCustomIdRaw(username, password, appId, customId, command)
                .then()
                .statusCode(200);
    }

    public static Response addGroupManagersByCustomIdRaw(String username, String password, String appId, String customId, IntegrationCustomAddGroupManagersCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/apps/{appId}/groups/custom/{customId}/managers/additions", appId, customId);
    }

    public static void removeGroupManagers(String username, String password, String groupId, IntegrationRemoveGroupManagersCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/groups/{groupId}/managers/deletions", groupId)
                .then()
                .statusCode(200);
    }

    public static void removeGroupManagersByCustomId(String username, String password, String appId, String customId, IntegrationCustomRemoveGroupManagersCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/apps/{appId}/groups/custom/{customId}/managers/deletions", appId, customId)
                .then()
                .statusCode(200);
    }

    public static void addGroupMembers(String username, String password, String groupId, IntegrationAddGroupMembersCommand command) {
        addGroupMembersRaw(username, password, groupId, command)
                .then()
                .statusCode(200);
    }

    public static Response addGroupMembersRaw(String username, String password, String groupId, IntegrationAddGroupMembersCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/groups/{groupId}/members/additions", groupId);
    }

    public static void addGroupMembersByCustomId(String username, String password, String appId, String customId, IntegrationCustomAddGroupMembersCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/apps/{appId}/groups/custom/{customId}/members/additions", appId, customId)
                .then()
                .statusCode(200);
    }

    public static void removeGroupMembers(String username, String password, String groupId, IntegrationRemoveGroupMembersCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/groups/{groupId}/members/deletions", groupId)
                .then()
                .statusCode(200);
    }

    public static void removeGroupMembersByCustomId(String username, String password, String appId, String customId, IntegrationCustomRemoveGroupMembersCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/apps/{appId}/groups/custom/{customId}/members/deletions", appId, customId)
                .then()
                .statusCode(200);
    }

    public static QIntegrationGroup fetchGroup(String username, String password, String groupId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/groups/{id}", groupId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static QIntegrationGroup fetchGroupByCustomId(String username, String password, String appId, String customId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/apps/{appId}/groups/custom/{customId}", appId, customId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }


    public static String createMember(String username, String password, IntegrationCreateMemberCommand command) {
        return createMemberRaw(username, password, command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static Response createMemberRaw(String username, String password, IntegrationCreateMemberCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .post("/integration/members");
    }

    public static List<QIntegrationListGroup> listGroups(String username, String password, String appId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/apps/{appId}/groups", appId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static void updateMemberCustomId(String username, String password, String memberId, IntegrationUpdateMemberCustomIdCommand command) {
        updateMemberCustomIdRaw(username, password, memberId, command)
                .then()
                .statusCode(200);
    }

    public static Response updateMemberCustomIdRaw(String username, String password, String memberId, IntegrationUpdateMemberCustomIdCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/members/{memberId}/custom-id", memberId);
    }

    public static void deleteMember(String username, String password, String memberId) {
        deleteMemberRaw(username, password, memberId)
                .then()
                .statusCode(200);
    }

    public static Response deleteMemberRaw(String username, String password, String memberId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .delete("/integration/members/{id}", memberId);
    }

    public static void deleteMemberByCustomId(String username, String password, String memberId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .delete("/integration/members/custom/{memberId}", memberId)
                .then()
                .statusCode(200);
    }

    public static void updateMemberInfo(String username, String password, String memberId, IntegrationUpdateMemberInfoCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/members/{memberId}", memberId)
                .then()
                .statusCode(200);
    }

    public static void updateMemberInfoByCustomId(String username, String password, String customId, IntegrationUpdateMemberInfoCommand command) {
        BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/members/custom/{customId}", customId)
                .then()
                .statusCode(200);
    }

    public static void activateMember(String username, String password, String memberId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/members/{memberId}/activation", memberId)
                .then()
                .statusCode(200);
    }

    public static void deactivateMember(String username, String password, String memberId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/members/{memberId}/deactivation", memberId)
                .then()
                .statusCode(200);
    }

    public static void activateMemberByCustomId(String username, String password, String customId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/members/custom/{customId}/activation", customId)
                .then()
                .statusCode(200);
    }

    public static void deactivateMemberByCustomId(String username, String password, String customId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/members/custom/{customId}/deactivation", customId)
                .then()
                .statusCode(200);
    }

    public static QIntegrationMember fetchMember(String username, String password, String memberId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/members/{memberId}", memberId)
                .then()
                .statusCode(200)
                .extract()
                .as(QIntegrationMember.class);
    }

    public static QIntegrationMember fetchMemberByCustomId(String username, String password, String customId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/members/custom/{customId}", customId)
                .then()
                .statusCode(200)
                .extract()
                .as(QIntegrationMember.class);
    }

    public static List<QIntegrationListMember> listMembers(String username, String password) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/members")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static Response createDepartmentRaw(String username, String password, IntegrationCreateDepartmentCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .post("/integration/departments");
    }

    public static String createDepartment(String username, String password, IntegrationCreateDepartmentCommand command) {
        return createDepartmentRaw(username, password, command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static Response updateDepartmentCustomIdRaw(String username, String password, String departmentId, IntegrationUpdateDepartmentCustomIdCommand command) {
        return BaseApiTest.givenBasic(username, password)
                .body(command)
                .when()
                .put("/integration/departments/{departmentId}/custom-id", departmentId);
    }

    public static void updateDepartmentCustomId(String username, String password, String departmentId, IntegrationUpdateDepartmentCustomIdCommand command) {
        updateDepartmentCustomIdRaw(username, password, departmentId, command)
                .then()
                .statusCode(200);
    }

    public static void addDepartmentMember(String username, String password, String departmentId, String memberId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/departments/{departmentId}/members/{memberId}", departmentId, memberId)
                .then()
                .statusCode(200);
    }

    public static void addDepartmentMemberByCustomId(String username, String password, String departmentCustomId, String memberCustomId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/departments/custom/{departmentCustomId}/members/{memberCustomId}", departmentCustomId, memberCustomId)
                .then()
                .statusCode(200);
    }

    public static void removeDepartmentMember(String username, String password, String departmentId, String memberId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .delete("/integration/departments/{departmentId}/members/{memberId}", departmentId, memberId)
                .then()
                .statusCode(200);
    }

    public static void removeDepartmentMemberByCustomId(String username, String password, String departmentCustomId, String memberCustomId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .delete("/integration/departments/custom/{departmentCustomId}/members/{memberCustomId}", departmentCustomId, memberCustomId)
                .then()
                .statusCode(200);
    }

    public static void addDepartmentManager(String username, String password, String departmentId, String memberId) {
        addDepartmentManagerRaw(username, password, departmentId, memberId)
                .then()
                .statusCode(200);
    }

    public static Response addDepartmentManagerRaw(String username, String password, String departmentId, String memberId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/departments/{departmentId}/managers/{memberId}", departmentId, memberId);
    }

    public static void addDepartmentManagerByCustomId(String username, String password, String departmentCustomId, String memberCustomId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .put("/integration/departments/custom/{departmentCustomId}/managers/{memberCustomId}", departmentCustomId, memberCustomId)
                .then()
                .statusCode(200);
    }

    public static void removeDepartmentManager(String username, String password, String departmentId, String memberId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .delete("/integration/departments/{departmentId}/managers/{memberId}", departmentId, memberId)
                .then()
                .statusCode(200);
    }

    public static void removeDepartmentManagerByCustomId(String username, String password, String departmentCustomId, String memberCustomId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .delete("/integration/departments/custom/{departmentCustomId}/managers/{memberCustomId}", departmentCustomId, memberCustomId)
                .then()
                .statusCode(200);
    }

    public static void deleteDepartment(String username, String password, String departmentId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .delete("/integration/departments/{id}", departmentId).then()
                .statusCode(200);
    }

    public static void deleteDepartmentByCustomId(String username, String password, String departmentCustomId) {
        BaseApiTest.givenBasic(username, password)
                .when()
                .delete("/integration/departments/custom/{id}", departmentCustomId).then()
                .statusCode(200);
    }

    public static List<QIntegrationListDepartment> listDepartments(String username, String password) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/departments")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static QIntegrationDepartment fetchDepartment(String username, String password, String departmentId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/departments/{departmentId}", departmentId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static QIntegrationDepartment fetchDepartmentByCustomId(String username, String password, String departmentCustomId) {
        return BaseApiTest.givenBasic(username, password)
                .when()
                .get("/integration/departments/custom/{departmentCustomId}", departmentCustomId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

}
