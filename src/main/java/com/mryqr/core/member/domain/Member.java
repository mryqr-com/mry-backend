package com.mryqr.core.member.domain;

import com.mryqr.common.domain.AggregateRoot;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.user.Role;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.core.member.domain.event.*;
import lombok.*;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.common.domain.user.Role.TENANT_ADMIN;
import static com.mryqr.common.domain.user.Role.TENANT_MEMBER;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.MEMBER_COLLECTION;
import static com.mryqr.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static java.time.LocalDate.now;
import static java.util.Set.copyOf;
import static java.util.stream.Stream.concat;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Document(MEMBER_COLLECTION)
@TypeAlias(MEMBER_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class Member extends AggregateRoot {
    private static final String WX_HEAD_IMAGE = "WX_HEAD_IMAGE";

    private String name;//名字
    private Role role;//角色
    private String mobile;//手机号，全局唯一，与email不能同时为空
    private String email;//邮箱，全局唯一，与mobile不能同时为空
    private String pcWxOpenId;//PC微信扫码登录后获得的openid，全局唯一
    private String mobileWxOpenId;//手机微信授权后获得的openid，全局唯一
    private String wxNickName;//微信昵称
    private String wxUnionId;//微信的Union ID
    private String password;//密码
    private boolean mobileIdentified;//是否已验证手机号
    private IdentityCard identityCard;//身份证
    private UploadedFile avatar;//avatar
    private String customId;//自定义编号，用于API查询用，租户下唯一
    private List<String> topAppIds;//顶置的app
    private FailedLoginCount failedLoginCount;//登录失败次数
    private boolean active;//是否启用
    private boolean tenantActive;//所在Tenant是否启用，通过EDA更新
    private List<String> departmentIds;

    public Member(String mobile, String email, String password, User user) {
        super(user.getMemberId(), user);
        this.name = user.getName();
        this.role = TENANT_ADMIN;
        this.mobile = mobile;
        if (isNotBlank(this.mobile)) {
            this.mobileIdentified = true;
        }
        this.email = email;
        this.password = password;
        this.failedLoginCount = FailedLoginCount.init();
        this.active = true;
        this.tenantActive = true;
        this.topAppIds = List.of();
        this.departmentIds = List.of();
        this.raiseEvent(new MemberCreatedEvent(this.getId(), user));
        this.addOpsLog("注册", user);
    }

    //正常添加
    public Member(String name, List<String> departmentIds, String mobile, String email, String password, String customId, User user) {
        super(newMemberId(), user);
        this.name = name;
        this.mobile = mobile;
        this.mobileIdentified = false;
        this.email = email;
        this.password = password;
        this.customId = customId;
        this.role = TENANT_MEMBER;
        this.failedLoginCount = FailedLoginCount.init();
        this.active = true;
        this.tenantActive = true;
        this.topAppIds = List.of();
        this.departmentIds = isNotEmpty(departmentIds) ? departmentIds : new ArrayList<>(0);

        this.raiseEvent(new MemberCreatedEvent(this.getId(), user));
        if (isNotEmpty(departmentIds)) {
            this.raiseEvent(new MemberDepartmentsChangedEvent(this.getId(), Set.of(), copyOf(departmentIds), user));
        }
        this.addOpsLog("新建", user);
    }

    public static String newMemberId() {
        return "MBR" + newSnowflakeId();
    }

    public void update(String name, List<String> departmentIds, String mobile, String email, User user) {
        if (!Objects.equals(this.name, name)) {
            this.name = name;
            raiseEvent(new MemberNameChangedEvent(this.getId(), name, user));
        }

        if (!Objects.equals(this.mobile, mobile)) {
            this.mobileIdentified = false;
        }

        this.mobile = mobile;
        this.email = email;

        if (departmentIds != null) {//传入null时，不做任何departmentIds的更新，主要用于不因为null而将已有的departmentIds更新没了
            Set<String> removedDepartmentIds = diff(this.departmentIds, departmentIds);
            Set<String> addedDepartmentIds = diff(departmentIds, this.departmentIds);
            if (isNotEmpty(removedDepartmentIds) || isNotEmpty(addedDepartmentIds)) {
                this.raiseEvent(new MemberDepartmentsChangedEvent(this.getId(), removedDepartmentIds, addedDepartmentIds, user));
            }
            this.departmentIds = departmentIds;
        }

        this.addOpsLog("更新信息", user);
    }

    private Set<String> diff(List<String> list1, List<String> list2) {
        HashSet<String> result = new HashSet<>(list1);
        result.removeAll(new HashSet<>(list2));
        return result;
    }

    public void updateRole(Role role, User user) {
        this.role = role;
        this.addOpsLog("更新角色为" + role.getRoleName(), user);
    }

    public void updateBaseSetting(String name, User user) {
        if (Objects.equals(this.name, name)) {
            return;
        }

        this.name = name;
        raiseEvent(new MemberNameChangedEvent(this.getId(), name, user));

        this.addOpsLog("更新基本设置", user);
    }

    public void changePassword(String password, User user) {
        if (Objects.equals(this.password, password)) {
            return;
        }

        this.password = password;
        this.addOpsLog("重置密码", user);
    }

    public void changeMobile(String mobile, User user) {
        if (Objects.equals(this.mobile, mobile)) {
            return;
        }

        this.mobile = mobile;
        this.mobileIdentified = true;
        this.addOpsLog("修改手机号为[" + mobile + "]", user);
    }

    public void identifyMobile(String mobile, User user) {
        if (isNotBlank(this.mobile) && !Objects.equals(this.mobile, mobile)) {
            throw new MryException(IDENTIFY_MOBILE_NOT_THE_SAME, "认证手机号与您当前账号的手机号不一致，无法完成认证。", "mobile", mobile);
        }

        this.mobile = mobile;
        this.mobileIdentified = true;
        this.addOpsLog("认证手机号：" + mobile, user);
    }

    public void topApp(String appId, User user) {
        topAppIds = Stream.concat(Stream.of(appId), this.topAppIds.stream()).distinct().limit(20).collect(toImmutableList());
        addOpsLog("顶置应用[" + appId + "]", user);
    }

    public void cancelTopApp(String appId, User user) {
        this.topAppIds = this.topAppIds.stream().filter(id -> !Objects.equals(id, appId)).collect(toImmutableList());
        addOpsLog("取消顶置应用[" + appId + "]", user);
    }

    public void activate(User user) {
        if (active) {
            return;
        }

        this.active = true;
        addOpsLog("启用", user);
    }

    public void deactivate(User user) {
        if (!active) {
            return;
        }

        this.active = false;
        addOpsLog("禁用", user);
    }

    public List<String> toppedAppIds() {
        return topAppIds;
    }

    public void updateAvatar(UploadedFile avatar, User user) {
        this.avatar = avatar;
        addOpsLog("更新头像", user);
    }

    public void deleteAvatar(User user) {
        this.avatar = null;
        addOpsLog("删除头像", user);
    }

    public void updateCustomId(String customId, User user) {
        if (Objects.equals(this.customId, customId)) {
            return;
        }

        this.customId = customId;
        addOpsLog("自定义编号改为[" + customId + "]", user);
    }

    public void bindMobileWx(String wxUnionId, String mobileWxOpenId, User user) {
        this.wxUnionId = wxUnionId;
        this.mobileWxOpenId = mobileWxOpenId;
        addOpsLog("绑定手机微信:" + wxUnionId, user);
    }

    public void bindPcWx(String wxUnionId, String pcWxOpenId, User user) {
        this.wxUnionId = wxUnionId;
        this.pcWxOpenId = pcWxOpenId;
        addOpsLog("绑定PC微信:" + wxUnionId, user);
    }

    public void unbindWx(User user) {
        //解绑时同时解绑手机端和PC端
        this.mobileWxOpenId = null;
        this.pcWxOpenId = null;
        this.wxUnionId = null;
        this.wxNickName = null;
        if (isAvatarFromWx()) {
            this.avatar = null;
        }
        this.addOpsLog("解绑微信", user);
    }

    public boolean updateMobileWxInfo(String mobileWxOpenId, String wxNickName, String avatarImageUrl, User user) {
        boolean updated = false;

        if (!Objects.equals(this.mobileWxOpenId, mobileWxOpenId)) {
            this.mobileWxOpenId = mobileWxOpenId;
            updated = true;
        }

        if (!Objects.equals(this.wxNickName, wxNickName)) {
            this.wxNickName = wxNickName;
            updated = true;
        }

        if (canUpdateWxAvatar() && isNotBlank(avatarImageUrl) && !Objects.equals(this.avatarImageUrl(), avatarImageUrl)) {
            this.avatar = wxAvatarOf(avatarImageUrl);
            updated = true;
        }

        if (updated) {
            addOpsLog("更新手机微信信息", user);
        }

        return updated;
    }

    public boolean updatePcWxInfo(String pcWxOpenId, String wxNickName, String avatarImageUrl, User user) {
        boolean updated = false;

        if (!Objects.equals(this.pcWxOpenId, pcWxOpenId)) {
            this.pcWxOpenId = pcWxOpenId;
            updated = true;
        }

        if (!Objects.equals(this.wxNickName, wxNickName)) {
            this.wxNickName = wxNickName;
            updated = true;
        }

        if (canUpdateWxAvatar() && isNotBlank(avatarImageUrl) && !Objects.equals(this.avatarImageUrl(), avatarImageUrl)) {
            this.avatar = wxAvatarOf(avatarImageUrl);
            updated = true;
        }

        if (updated) {
            addOpsLog("更新PC微信信息", user);
        }

        return updated;
    }

    private String avatarImageUrl() {
        return this.avatar != null ? this.avatar.getFileUrl() : null;
    }

    public boolean isWxBound() {
        return isNotBlank(this.wxUnionId);
    }

    private UploadedFile wxAvatarOf(String avatarImageUrl) {
        return UploadedFile.builder()
                .id(newShortUuid())
                .name(WX_HEAD_IMAGE)
                .type("image/jpeg")
                .fileUrl(avatarImageUrl)
                .size(100)
                .build();
    }

    private boolean canUpdateWxAvatar() {
        return this.avatar == null || Objects.equals(this.avatar.getName(), WX_HEAD_IMAGE);
    }

    private boolean isAvatarFromWx() {
        return this.avatar != null && Objects.equals(this.avatar.getName(), WX_HEAD_IMAGE);
    }

    public boolean isMemberOf(String departmentId) {
        return this.departmentIds.contains(departmentId);
    }

    public void addToDepartment(String departmentId, User user) {
        if (this.departmentIds.contains(departmentId)) {
            return;
        }

        this.departmentIds = concat(departmentIds.stream(), Stream.of(departmentId)).distinct().collect(toImmutableList());
        raiseEvent(new MemberAddedToDepartmentEvent(this.getId(), departmentId, user));
        addOpsLog("添加到部门[" + departmentId + "]", user);
    }

    public void removeFromDepartment(String departmentId, User user) {
        if (!this.departmentIds.contains(departmentId)) {
            return;
        }

        this.departmentIds = this.departmentIds.stream().filter(id -> !Objects.equals(id, departmentId)).distinct().collect(toImmutableList());
        raiseEvent(new MemberRemovedFromDepartmentEvent(this.getId(), departmentId, user));
        addOpsLog("从部门[" + departmentId + "]移除", user);
    }

    public void recordFailedLogin() {
        this.failedLoginCount.recordFailedLogin();
    }

    public void checkActive() {
        if (this.failedLoginCount.isLocked()) {
            throw new MryException(MEMBER_ALREADY_LOCKED, "当前用户已经被锁定，次日零点系统将自动解锁。", mapOf("memberId", this.getId()));
        }

        if (!this.active) {
            throw new MryException(MEMBER_ALREADY_DEACTIVATED, "当前用户已经被禁用。", mapOf("memberId", this.getId()));
        }

        if (!this.tenantActive) {
            throw new MryException(TENANT_ALREADY_DEACTIVATED, "当前账户已经被禁用。",
                    mapOf("memberId", this.getId(), "tenantId", this.getTenantId()));
        }
    }

    public User toUser() {
        return User.humanUser(this.getId(), this.getName(), this.getTenantId(), this.getRole());
    }

    public void onDelete(User user) {
        this.raiseEvent(new MemberDeletedEvent(this.getId(), user));
    }

    @Getter
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor(access = PRIVATE)
    public static class FailedLoginCount {
        private static final int MAX_ALLOWED_FAILED_LOGIN_PER_DAY = 30;

        private LocalDate date;
        private int count;

        public static FailedLoginCount init() {
            return FailedLoginCount.builder().date(now()).count(0).build();
        }

        private void recordFailedLogin() {
            LocalDate now = now();
            if (now.equals(date)) {
                count++;
            } else {
                this.date = now;
                this.count = 0;
            }
        }

        private boolean isLocked() {
            return now().equals(date) && this.count >= MAX_ALLOWED_FAILED_LOGIN_PER_DAY;
        }
    }
}
