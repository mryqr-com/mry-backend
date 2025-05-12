package com.mryqr.core.member;

import com.mryqr.BaseApiTest;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.common.utils.ReturnId;
import com.mryqr.core.login.LoginApi;
import com.mryqr.core.member.command.ChangeMyMobileCommand;
import com.mryqr.core.member.command.ChangeMyPasswordCommand;
import com.mryqr.core.member.command.CreateMemberCommand;
import com.mryqr.core.member.command.FindbackPasswordCommand;
import com.mryqr.core.member.command.IdentifyMyMobileCommand;
import com.mryqr.core.member.command.ResetMemberPasswordCommand;
import com.mryqr.core.member.command.UpdateMemberInfoCommand;
import com.mryqr.core.member.command.UpdateMemberRoleCommand;
import com.mryqr.core.member.command.UpdateMyAvatarCommand;
import com.mryqr.core.member.command.UpdateMyBaseSettingCommand;
import com.mryqr.core.member.command.importmember.MemberImportResponse;
import com.mryqr.core.member.query.ListMyManagedMembersQuery;
import com.mryqr.core.member.query.QListMember;
import com.mryqr.core.member.query.QMemberBaseSetting;
import com.mryqr.core.member.query.QMemberInfo;
import com.mryqr.core.member.query.QMemberReference;
import com.mryqr.core.member.query.profile.QClientMemberProfile;
import com.mryqr.core.member.query.profile.QConsoleMemberProfile;
import com.mryqr.utils.CreateMemberResponse;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.io.File;
import java.util.List;

import static com.mryqr.utils.RandomTestFixture.rEmail;
import static com.mryqr.utils.RandomTestFixture.rMemberName;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rPassword;

public class MemberApi {
    public static Response createMemberRaw(String jwt, CreateMemberCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/members");
    }

    public static String createMember(String jwt, String name, String mobile, String password) {
        CreateMemberCommand command = CreateMemberCommand.builder()
                .name(name)
                .mobile(mobile)
                .password(password)
                .departmentIds(List.of())
                .build();

        return createMember(jwt, command);
    }

    public static String createMember(String jwt, String name, String mobile, String email, String password) {
        CreateMemberCommand command = CreateMemberCommand.builder()
                .name(name)
                .mobile(mobile)
                .email(email)
                .password(password)
                .departmentIds(List.of())
                .build();

        return createMember(jwt, command);
    }

    public static String createMember(String jwt, CreateMemberCommand command) {
        return createMemberRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static String createMemberUnderDepartment(String jwt, String... departmentIds) {
        CreateMemberCommand command = CreateMemberCommand.builder()
                .name(rMemberName())
                .mobile(rMobile())
                .email(rEmail())
                .password(rPassword())
                .departmentIds(List.of(departmentIds))
                .build();

        return createMember(jwt, command);
    }

    public static String createMember(String jwt) {
        CreateMemberCommand command = CreateMemberCommand.builder()
                .name(rMemberName())
                .mobile(rMobile())
                .email(rEmail())
                .password(rPassword())
                .departmentIds(List.of())
                .build();

        return createMember(jwt, command);
    }

    public static Response updateMemberRaw(String jwt, String memberId, UpdateMemberInfoCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/members/{memberId}", memberId);

    }

    public static Response importMembersRaw(String jwt, File file) {
        return BaseApiTest.given(jwt)
                .contentType("multipart/form-data")
                .multiPart("file", file)
                .when()
                .post("/members/import");
    }

    public static MemberImportResponse importMembers(String jwt, File file) {
        return importMembersRaw(jwt, file)
                .then()
                .statusCode(200)
                .extract()
                .as(MemberImportResponse.class);
    }

    public static void updateMember(String jwt, String memberId, UpdateMemberInfoCommand command) {
        updateMemberRaw(jwt, memberId, command)
                .then()
                .statusCode(200);
    }

    public static void updateMemberRole(String jwt, String memberId, UpdateMemberRoleCommand command) {
        updateMemberRoleRaw(jwt, memberId, command)
                .then()
                .statusCode(200);
    }

    public static Response updateMemberRoleRaw(String jwt, String memberId, UpdateMemberRoleCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/members/{memberId}/role", memberId);

    }

    public static void topApp(String jwt, String appId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/members/me/top-apps/{appId}", appId)
                .then()
                .statusCode(200);
    }

    public static void cancelTopApp(String jwt, String appId) {
        BaseApiTest.given(jwt)
                .when()
                .delete("/members/me/top-apps/{appId}", appId)
                .then()
                .statusCode(200);
    }

    public static void deactivateMember(String jwt, String memberId) {
        deactivateMemberRaw(jwt, memberId)
                .then()
                .statusCode(200);
    }

    public static Response deactivateMemberRaw(String jwt, String memberId) {
        return BaseApiTest.given(jwt)
                .when()
                .put("/members/{memberId}/deactivation", memberId);
    }

    public static void activateMember(String jwt, String memberId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/members/{memberId}/activation", memberId)
                .then()
                .statusCode(200);
    }

    public static Response deleteMemberRaw(String jwt, String memberId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/members/{memberId}", memberId);
    }

    public static void deleteMember(String jwt, String memberId) {
        deleteMemberRaw(jwt, memberId)
                .then()
                .statusCode(200);
    }

    public static void unbindWx(String jwt, String memberId) {
        BaseApiTest.given(jwt)
                .when()
                .delete("/members/{memberId}/wx", memberId)
                .then()
                .statusCode(200);
    }

    public static void resetPassword(String jwt, String memberId, ResetMemberPasswordCommand command) {
        BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/members/{memberId}/password", memberId)
                .then()
                .statusCode(200);
    }

    public static Response findbackPasswordRaw(FindbackPasswordCommand command) {
        return BaseApiTest.given()
                .body(command)
                .when()
                .post("/members/findback-password");
    }

    public static void findbackPassword(FindbackPasswordCommand command) {
        findbackPasswordRaw(command)
                .then()
                .statusCode(200);
    }

    public static Response updateMyBaseSettingRaw(String jwt, UpdateMyBaseSettingCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/members/me/base-setting");
    }

    public static void updateMyBaseSetting(String jwt, UpdateMyBaseSettingCommand command) {
        updateMyBaseSettingRaw(jwt, command)
                .then()
                .statusCode(200);
    }

    public static void updateMyAvatar(String jwt, UpdateMyAvatarCommand command) {
        BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/members/me/avatar")
                .then()
                .statusCode(200);
    }

    public static void deleteMyAvatar(String jwt) {
        BaseApiTest.given(jwt)
                .when()
                .delete("/members/me/avatar")
                .then()
                .statusCode(200);
    }

    public static Response changeMyMobileRaw(String jwt, ChangeMyMobileCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/members/me/mobile");
    }

    public static void changeMyMobile(String jwt, ChangeMyMobileCommand command) {
        changeMyMobileRaw(jwt, command)
                .then()
                .statusCode(200);
    }

    public static Response identifyMyMobileRaw(String jwt, IdentifyMyMobileCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/members/me/mobile-identification");
    }

    public static void identifyMyMobile(String jwt, IdentifyMyMobileCommand command) {
        identifyMyMobileRaw(jwt, command)
                .then()
                .statusCode(200);
    }

    public static Response changeMyPasswordRaw(String jwt, ChangeMyPasswordCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/members/me/password");

    }

    public static void changeMyPassword(String jwt, ChangeMyPasswordCommand command) {
        changeMyPasswordRaw(jwt, command)
                .then()
                .statusCode(200);
    }


    public static void unbindMyWx(String jwt) {
        BaseApiTest.given(jwt)
                .when()
                .delete("/members/me/wx")
                .then()
                .statusCode(200);
    }


    public static PagedList<QListMember> listMembers(String jwt, String departmentId, String search, String sortedBy, boolean ascSort, int pageIndex, int pageSize) {
        return BaseApiTest.given(jwt)
                .body(ListMyManagedMembersQuery.builder().departmentId(departmentId).search(search).sortedBy(sortedBy).ascSort(ascSort).pageIndex(pageIndex).pageSize(pageSize).build())
                .when()
                .post("/members/my-managed-members")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    private static Response allMemberReferencesRaw(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/members/all-references");
    }

    public static List<QMemberReference> allMemberReferences(String jwt) {
        return allMemberReferencesRaw(jwt)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static Response allMemberReferencesRaw(String jwt, String tenantId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/members/all-references/{tenantId}", tenantId);
    }

    public static List<QMemberReference> allMemberReferences(String jwt, String tenantId) {
        return allMemberReferencesRaw(jwt, tenantId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static QConsoleMemberProfile myProfile(String jwt) {
        return myProfileRaw(jwt)
                .then()
                .statusCode(200)
                .extract()
                .as(QConsoleMemberProfile.class);
    }

    public static Response myProfileRaw(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/members/me");
    }

    public static QClientMemberProfile myClientProfile(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/members/client/me")
                .then()
                .statusCode(200)
                .extract()
                .as(QClientMemberProfile.class);
    }

    public static QMemberInfo myMemberInfo(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/members/me/info")
                .then()
                .statusCode(200)
                .extract()
                .as(QMemberInfo.class);
    }

    public static QMemberBaseSetting myBaseSetting(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/members/me/base-setting")
                .then()
                .statusCode(200)
                .extract()
                .as(QMemberBaseSetting.class);
    }

    public static CreateMemberResponse createMemberAndLogin(String jwt, String name, String mobile, String password) {
        String memberId = createMember(jwt, name, mobile, password);
        String memberJwt = LoginApi.loginWithMobileOrEmail(mobile, password);
        return new CreateMemberResponse(memberId, name, mobile, password, memberJwt);
    }

    public static CreateMemberResponse createMemberAndLogin(String jwt) {
        return createMemberAndLogin(jwt, rMemberName(), rMobile(), rPassword());
    }
}
