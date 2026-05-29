package com.mryqr.core.group.domain;

import com.mryqr.common.domain.AggregateRoot;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.department.domain.Department;
import com.mryqr.core.group.domain.event.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.common.exception.ErrorCode.GROUP_SYNCED;
import static com.mryqr.common.exception.ErrorCode.MAX_GROUP_MANAGER_REACHED;
import static com.mryqr.common.utils.MryConstants.GROUP_COLLECTION;
import static com.mryqr.common.utils.MryConstants.MAX_GROUP_MANAGER_SIZE;
import static com.mryqr.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static java.util.Set.copyOf;
import static java.util.Set.of;
import static java.util.stream.Stream.concat;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Document(GROUP_COLLECTION)
@TypeAlias(GROUP_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class Group extends AggregateRoot {
    private String name;//名称
    private String appId;//所在的app
    private List<String> managers;//管理员
    private List<String> members;//普通成员
    private boolean archived;//是否归档，归档后group下的资源将不再显示在运营端，但是依然可以完成其下qr的扫码
    private String customId;//自定义编号，在App下唯一
    private boolean active;//是否启用
    private String departmentId;//由哪个部门同步而来

    public Group(String name, App app, User user) {
        super(newGroupId(), app.getTenantId(), user);
        init(name, app.getId(), user);
    }

    public Group(String name, App app, String customId, User user) {
        super(newGroupId(), app.getTenantId(), user);
        init(name, app.getId(), user);
        this.customId = customId;
    }

    public Group(String id, String name, App app, User user) {
        super(id, app.getTenantId(), user);
        init(name, app.getId(), user);
    }

    public Group(Department department, String appId, User user) {
        super(newGroupId(), department.getTenantId(), user);
        init(department.getName(), appId, user);
        this.departmentId = department.getId();
        this.managers = department.getManagers().stream().distinct().collect(toImmutableList());
    }

    private void init(String name, String appId, User user) {
        this.name = name;
        this.appId = appId;
        this.managers = List.of();
        this.members = List.of();
        this.active = true;
        this.addOpsLog("新建", user);
        raiseEvent(new GroupCreatedEvent(this.getId(), this.getAppId(), user));
    }

    public static String newGroupId() {
        return "GRP" + newSnowflakeId();
    }

    public void rename(String newName, User user) {
        if (isSynced()) {
            throw new MryException(GROUP_SYNCED, "重命名失败，已设置从部门同步。", "groupId", this.getId());
        }

        if (this.name.equals(newName)) {
            return;
        }

        this.name = newName;
        addOpsLog("重命名为：" + this.name, user);
    }

    public void addMembers(List<String> memberIds, User user) {
        if (isSynced()) {
            throw new MryException(GROUP_SYNCED, "无法添加成员，已设置从部门同步。", "groupId", this.getId());
        }

        this.members = concat(members.stream(), memberIds.stream()).distinct().collect(toImmutableList());
        addOpsLog("设置成员", user);
    }

    public void removeMember(String memberId, User user) {
        if (isSynced()) {
            throw new MryException(GROUP_SYNCED, "无法移除成员，已设置从部门同步。", "groupId", this.getId());
        }

        this.members = this.members.stream().filter(id -> !Objects.equals(id, memberId)).collect(toImmutableList());

        List<String> remainManagers = this.managers.stream().filter(id -> !Objects.equals(id, memberId)).collect(toImmutableList());
        if (!of(managers).equals(of(remainManagers))) {
            raiseEvent(new GroupManagersChangedEvent(this.getId(), this.getAppId(), user));
        }

        this.managers = remainManagers;
        addOpsLog("移除成员", user);
    }

    public void removeMembers(List<String> memberIds, User user) {
        if (isSynced()) {
            throw new MryException(GROUP_SYNCED, "无法移除成员，已设置从部门同步。", "groupId", this.getId());
        }

        this.members = this.members.stream().filter(id -> !memberIds.contains(id)).collect(toImmutableList());
        List<String> remainManagers = this.managers.stream().filter(id -> !memberIds.contains(id)).collect(toImmutableList());

        if (!of(managers).equals(of(remainManagers))) {
            raiseEvent(new GroupManagersChangedEvent(this.getId(), this.getAppId(), user));
        }

        this.managers = remainManagers;
        addOpsLog("移除成员(多)", user);
    }

    public void addManager(String memberId, User user) {
        if (isSynced()) {
            throw new MryException(GROUP_SYNCED, "无法添加管理员，已设置从部门同步。", "groupId", this.getId());
        }

        if (this.managers.contains(memberId)) {
            return;
        }

        if (!this.members.contains(memberId)) {
            this.members = concat(members.stream(), Stream.of(memberId)).distinct().collect(toImmutableList());
        }

        this.managers = concat(this.managers.stream(), Stream.of(memberId)).distinct().collect(toImmutableList());
        if (this.managers.size() > MAX_GROUP_MANAGER_SIZE) {
            throw new MryException(MAX_GROUP_MANAGER_REACHED,
                    "无法添加管理员，管理员数量已达所允许的最大值(" + MAX_GROUP_MANAGER_SIZE + "个)。", "groupId", this.getId());
        }

        raiseEvent(new GroupManagersChangedEvent(this.getId(), this.getAppId(), user));
        addOpsLog("添加管理员", user);
    }

    public void addManagers(List<String> managerIds, User user) {
        if (isSynced()) {
            throw new MryException(GROUP_SYNCED, "无法添加管理员，已设置从部门同步。", "groupId", this.getId());
        }

        if (copyOf(this.managers).containsAll(managerIds)) {
            return;
        }

        this.members = concat(members.stream(), managerIds.stream()).distinct().collect(toImmutableList());

        List<String> resultManagers = concat(this.managers.stream(), managerIds.stream()).distinct().collect(toImmutableList());
        if (resultManagers.size() > MAX_GROUP_MANAGER_SIZE) {
            throw new MryException(MAX_GROUP_MANAGER_REACHED,
                    "无法添加管理员，管理员数量已达所允许的最大值(" + MAX_GROUP_MANAGER_SIZE + "个)。", "groupId", this.getId());
        }

        if (!of(managers).equals(of(resultManagers))) {
            raiseEvent(new GroupManagersChangedEvent(this.getId(), this.getAppId(), user));
        }

        this.managers = resultManagers;
        addOpsLog("添加管理员(多)", user);
    }

    public void removeManager(String memberId, User user) {
        if (isSynced()) {
            throw new MryException(GROUP_SYNCED, "无法移除管理员，已设置从部门同步。", "groupId", this.getId());
        }

        if (!this.managers.contains(memberId)) {
            return;
        }

        this.managers = this.managers.stream().filter(id -> !Objects.equals(id, memberId)).distinct().collect(toImmutableList());
        raiseEvent(new GroupManagersChangedEvent(this.getId(), this.getAppId(), user));
        addOpsLog("移除管理员", user);
    }

    public void removeManagers(List<String> managerIds, User user) {
        if (isSynced()) {
            throw new MryException(GROUP_SYNCED, "无法移除管理员，已设置从部门同步。", "groupId", this.getId());
        }

        List<String> resultManagers = this.managers.stream().filter(id -> !managerIds.contains(id)).collect(toImmutableList());

        if (!of(managers).equals(of(resultManagers))) {
            raiseEvent(new GroupManagersChangedEvent(this.getId(), this.getAppId(), user));
        }

        this.managers = resultManagers;
        addOpsLog("移除管理员(多)", user);
    }

    public void archive(User user) {
        if (this.archived) {
            return;
        }

        this.archived = true;
        addOpsLog("归档", user);
    }

    public void unArchive(User user) {
        if (!this.archived) {
            return;
        }

        this.archived = false;
        addOpsLog("解除归档", user);
    }

    public void updateCustomId(String customId, User user) {
        if (Objects.equals(this.customId, customId)) {
            return;
        }

        this.customId = customId;
        addOpsLog("自定义编号改为[" + customId + "]", user);
    }

    public void activate(User user) {
        if (active) {
            return;
        }

        this.active = true;
        raiseEvent(new GroupActivatedEvent(this.getId(), this.getAppId(), user));
        addOpsLog("启用", user);
    }

    public void deactivate(User user) {
        if (!active) {
            return;
        }

        this.active = false;
        raiseEvent(new GroupDeactivatedEvent(this.getId(), this.getAppId(), user));
        addOpsLog("禁用", user);
    }

    public boolean containsManager(String memberId) {
        return this.managers.contains(memberId);
    }

    public boolean containsMember(String memberId) {
        return containsManager(memberId) || this.members.contains(memberId);
    }

    public List<String> allManagerIds() {
        return managers;
    }

    public boolean isVisible() {
        return this.active && !this.archived;
    }

    public void syncDepartment(Department department, User user) {
        this.departmentId = department.getId();
        this.name = department.getName();

        if (!of(department.getManagers()).equals(of(this.managers))) {
            raiseEvent(new GroupManagersChangedEvent(this.getId(), this.getAppId(), user));
        }

        this.managers = department.getManagers().stream().distinct().collect(toImmutableList());
        this.members = concat(this.members.stream(), this.managers.stream()).distinct().collect(toImmutableList());

        addOpsLog("同步部门", user);
    }

    public void syncDepartment(Department department, Collection<String> members, User user) {
        this.departmentId = department.getId();
        this.name = department.getName();

        if (!of(department.getManagers()).equals(of(this.managers))) {
            raiseEvent(new GroupManagersChangedEvent(this.getId(), this.getAppId(), user));
        }

        this.managers = department.getManagers().stream().distinct().collect(toImmutableList());
        this.members = concat(members.stream(), department.getManagers().stream()).distinct().collect(toImmutableList());

        addOpsLog("同步部门（包括成员）", user);
    }

    public void resetDepartment(User user) {
        this.departmentId = null;
        addOpsLog("重置部门", user);
    }

    public boolean isSynced() {
        return isNotBlank(this.departmentId);
    }

    public void onDelete(User user) {
        if (isSynced()) {
            throw new MryException(GROUP_SYNCED, "删除失败，已设置从部门同步。", "groupId", this.getId());
        }

        raiseEvent(new GroupDeletedEvent(this.getId(), this.getAppId(), user));
    }

}
