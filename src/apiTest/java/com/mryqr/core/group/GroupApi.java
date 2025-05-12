package com.mryqr.core.group;

import com.mryqr.BaseApiTest;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.common.utils.ReturnId;
import com.mryqr.core.group.command.AddGroupManagersCommand;
import com.mryqr.core.group.command.AddGroupMembersCommand;
import com.mryqr.core.group.command.CreateGroupCommand;
import com.mryqr.core.group.command.RenameGroupCommand;
import com.mryqr.core.group.query.ListGroupQrsQuery;
import com.mryqr.core.group.query.QGroupMembers;
import com.mryqr.core.group.query.QGroupQr;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.util.List;

import static com.mryqr.utils.RandomTestFixture.rGroupName;

public class GroupApi {
    public static Response createGroupRaw(String jwt, CreateGroupCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/groups");
    }

    public static String createGroup(String jwt, CreateGroupCommand command) {
        return createGroupRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static String createGroup(String jwt, String appId, String groupName) {
        return createGroup(jwt, CreateGroupCommand.builder().appId(appId).name(groupName).build());
    }

    public static String createGroup(String jwt, String appId) {
        return createGroup(jwt, CreateGroupCommand.builder().appId(appId).name(rGroupName()).build());
    }

    public static String createGroupWithParent(String jwt, String appId, String parentGroupId) {
        return createGroup(jwt, CreateGroupCommand.builder().appId(appId).name(rGroupName()).parentGroupId(parentGroupId).build());
    }

    public static Response renameGroupRaw(String jwt, String groupId, RenameGroupCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/groups/{groupId}/name", groupId);
    }

    public static void renameGroup(String jwt, String groupId, RenameGroupCommand command) {
        renameGroupRaw(jwt, groupId, command)
                .then()
                .statusCode(200);
    }

    public static void renameGroup(String jwt, String groupId, String name) {
        renameGroup(jwt, groupId, RenameGroupCommand.builder().name(name).build());
    }

    public static Response addGroupMembersRaw(String jwt, String groupId, AddGroupMembersCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/groups/{groupId}/members", groupId);
    }

    public static void addGroupMembers(String jwt, String groupId, AddGroupMembersCommand command) {
        addGroupMembersRaw(jwt, groupId, command).then().statusCode(200);
    }

    public static void addGroupMembers(String jwt, String groupId, String... memberIds) {
        addGroupMembersRaw(jwt, groupId, AddGroupMembersCommand.builder().memberIds(List.of(memberIds)).build()).then().statusCode(200);
    }

    public static Response removeGroupMemberRaw(String jwt, String groupId, String memberId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/groups/{groupId}/members/{memberId}", groupId, memberId);
    }

    public static void removeGroupMember(String jwt, String groupId, String memberId) {
        removeGroupMemberRaw(jwt, groupId, memberId).then().statusCode(200);
    }

    public static Response addGroupManagerRaw(String jwt, String groupId, String memberId) {
        return BaseApiTest.given(jwt)
                .when()
                .put("/groups/{groupId}/managers/{memberId}", groupId, memberId);
    }

    public static void addGroupManager(String jwt, String groupId, String memberId) {
        addGroupManagerRaw(jwt, groupId, memberId).then().statusCode(200);
    }

    public static Response addGroupManagersRaw(String jwt, String groupId, AddGroupManagersCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/groups/{groupId}/managers", groupId);
    }

    public static void addGroupManagers(String jwt, String groupId, AddGroupManagersCommand command) {
        addGroupManagersRaw(jwt, groupId, command).then().statusCode(200);
    }

    public static void addGroupManagers(String jwt, String groupId, String... memberIds) {
        addGroupManagers(jwt, groupId, AddGroupManagersCommand.builder().memberIds(List.of(memberIds)).build());
    }

    public static Response removeGroupManagerRaw(String jwt, String groupId, String memberId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/groups/{groupId}/managers/{memberId}", groupId, memberId);
    }

    public static void removeGroupManager(String jwt, String groupId, String memberId) {
        removeGroupManagerRaw(jwt, groupId, memberId).then().statusCode(200);
    }

    public static Response deleteGroupRaw(String jwt, String groupId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/groups/{id}", groupId);
    }

    public static void deleteGroup(String jwt, String groupId) {
        deleteGroupRaw(jwt, groupId)
                .then()
                .statusCode(200);
    }

    public static Response archiveGroupRaw(String jwt, String groupId) {
        return BaseApiTest.given(jwt)
                .when()
                .put("/groups/{id}/archive", groupId);
    }

    public static void archiveGroup(String jwt, String groupId) {
        archiveGroupRaw(jwt, groupId)
                .then()
                .statusCode(200);
    }

    public static void unArchiveGroup(String jwt, String groupId) {
        BaseApiTest.given(jwt)
                .when()
                .delete("/groups/{id}/archive", groupId)
                .then()
                .statusCode(200);
    }

    public static void activateGroup(String jwt, String groupId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/groups/{groupId}/activation", groupId)
                .then()
                .statusCode(200);
    }

    public static void deactivateGroup(String jwt, String groupId) {
        deactivateGroupRaw(jwt, groupId)
                .then()
                .statusCode(200);
    }

    public static Response deactivateGroupRaw(String jwt, String groupId) {
        return BaseApiTest.given(jwt)
                .when()
                .put("/groups/{groupId}/deactivation", groupId);
    }

    public static Response allGroupMembersRaw(String jwt, String groupId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/groups/{groupId}/members", groupId);
    }

    public static QGroupMembers allGroupMembers(String jwt, String groupId) {
        return allGroupMembersRaw(jwt, groupId)
                .then()
                .statusCode(200)
                .extract()
                .as(QGroupMembers.class);
    }

    public static Response listGroupQrsRaw(String jwt, String groupId, ListGroupQrsQuery command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/groups/{groupId}/qrs", groupId);
    }

    public static PagedList<QGroupQr> listGroupQrs(String jwt, String groupId, ListGroupQrsQuery command) {
        return listGroupQrsRaw(jwt, groupId, command)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }
}
