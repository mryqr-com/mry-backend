package com.mryqr.core.department.domain;

import com.mryqr.common.domain.AggregateRoot;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.core.department.domain.event.DepartmentCreatedEvent;
import com.mryqr.core.department.domain.event.DepartmentDeletedEvent;
import com.mryqr.core.department.domain.event.DepartmentManagersChangedEvent;
import com.mryqr.core.department.domain.event.DepartmentRenamedEvent;
import com.mryqr.core.member.domain.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.common.exception.ErrorCode.MAX_DEPARTMENT_MANAGER_REACHED;
import static com.mryqr.common.exception.ErrorCode.NOT_DEPARTMENT_MEMBER;
import static com.mryqr.common.utils.MryConstants.DEPARTMENT_COLLECTION;
import static com.mryqr.common.utils.MryConstants.MAX_GROUP_MANAGER_SIZE;
import static com.mryqr.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static java.util.stream.Stream.concat;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Document(DEPARTMENT_COLLECTION)
@TypeAlias(DEPARTMENT_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class Department extends AggregateRoot {
    private String name;
    private List<String> managers;
    private String customId;

    public Department(String name, String customId, User user) {
        super(newDepartmentId(), user);
        this.name = name;
        this.managers = List.of();
        this.customId = customId;
        raiseEvent(new DepartmentCreatedEvent(this.getId(), user));
        addOpsLog("新建", user);
    }

    public static String newDepartmentId() {
        return "DPT" + newSnowflakeId();
    }

    public void addManager(Member member, User user) {
        if (this.managers.contains(member.getId())) {
            return;
        }

        if (!member.isMemberOf(this.getId())) {
            throw new MryException(NOT_DEPARTMENT_MEMBER, "设置部门管理员失败，管理员必须首先为部门成员。", "memberId", member.getId(), "departmentId", this.getId());
        }

        this.managers = concat(this.managers.stream(), Stream.of(member.getId())).distinct().collect(toImmutableList());
        if (this.managers.size() > MAX_GROUP_MANAGER_SIZE) {
            throw new MryException(MAX_DEPARTMENT_MANAGER_REACHED,
                    "无法添加管理员，管理员数量已达所允许的最大值(" + MAX_GROUP_MANAGER_SIZE + "个)。", "departmentId", this.getId());
        }

        raiseEvent(new DepartmentManagersChangedEvent(this.getId(), user));
        addOpsLog("添加管理员", user);
    }

    public void removeManager(String memberId, User user) {
        if (!this.managers.contains(memberId)) {
            return;
        }

        this.managers = this.managers.stream().filter(id -> !Objects.equals(id, memberId)).distinct().collect(toImmutableList());
        raiseEvent(new DepartmentManagersChangedEvent(this.getId(), user));
        addOpsLog("移除管理员", user);
    }

    public void rename(String name, User user) {
        if (Objects.equals(this.name, name)) {
            return;
        }

        this.name = name;
        raiseEvent(new DepartmentRenamedEvent(this.getId(), user));
        addOpsLog("重命名为[" + name + "]", user);
    }

    public void updateCustomId(String customId, User user) {
        if (Objects.equals(this.customId, customId)) {
            return;
        }

        this.customId = customId;
        addOpsLog("设置自定义编号为[" + customId + "]", user);
    }

    public void onDelete(User user) {
        this.raiseEvent(new DepartmentDeletedEvent(this.getId(), user));
    }
}
