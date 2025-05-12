package com.mryqr.core.departmenthierarchy.domain;

import com.mryqr.core.common.domain.AggregateRoot;
import com.mryqr.core.common.domain.idnode.IdTree;
import com.mryqr.core.common.domain.idnode.IdTreeHierarchy;
import com.mryqr.core.common.domain.idnode.exception.IdNodeLevelOverflowException;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.departmenthierarchy.domain.event.DepartmentHierarchyChangedEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static com.mryqr.core.common.exception.ErrorCode.DEPARTMENT_HIERARCHY_TOO_DEEP;
import static com.mryqr.core.common.exception.ErrorCode.DEPARTMENT_NOT_FOUND;
import static com.mryqr.core.common.utils.MryConstants.DEPARTMENT_HIERARCHY_COLLECTION;
import static com.mryqr.core.common.utils.MryConstants.MAX_GROUP_HIERARCHY_LEVEL;
import static com.mryqr.core.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Document(DEPARTMENT_HIERARCHY_COLLECTION)
@TypeAlias(DEPARTMENT_HIERARCHY_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class DepartmentHierarchy extends AggregateRoot {
    private IdTree idTree;
    private IdTreeHierarchy hierarchy;

    public DepartmentHierarchy(User user) {
        super(newDepartmentHierarchyId(), user);
        this.idTree = new IdTree(new ArrayList<>(0));
        this.buildHierarchy();
        addOpsLog("新建", user);
    }

    public static String newDepartmentHierarchyId() {
        return "DHC" + newSnowflakeId();
    }

    public void update(IdTree idTree, User user) {
        this.idTree = idTree;
        this.buildHierarchy();
        raiseEvent(new DepartmentHierarchyChangedEvent(this.getTenantId(), user));
        addOpsLog("更新层级", user);
    }

    public void addDepartment(String parentDepartmentId, String departmentId, User user) {
        if (isNotBlank(parentDepartmentId)) {
            if (!containsDepartmentId(parentDepartmentId)) {
                throw new MryException(DEPARTMENT_NOT_FOUND, "未找到部门。", "departmentId", parentDepartmentId);
            }

            if (this.hierarchy.levelOf(parentDepartmentId) >= MAX_GROUP_HIERARCHY_LEVEL) {
                throw new MryException(DEPARTMENT_HIERARCHY_TOO_DEEP, "添加失败，部门层级最多不能超过5层。", "tenantId", this.getTenantId());
            }
        }

        this.idTree.addNode(parentDepartmentId, departmentId);
        this.buildHierarchy();
        raiseEvent(new DepartmentHierarchyChangedEvent(this.getTenantId(), user));
        addOpsLog("添加部门[" + departmentId + "]", user);
    }

    public void removeDepartment(String departmentId, User user) {
        this.idTree.removeNode(departmentId);
        this.buildHierarchy();
        raiseEvent(new DepartmentHierarchyChangedEvent(this.getTenantId(), user));
        addOpsLog("删除部门[" + departmentId + "]", user);
    }

    public Set<String> directChildDepartmentIdsUnder(String parentDepartmentId) {
        return this.hierarchy.directChildIdsUnder(parentDepartmentId);
    }

    public Set<String> allSubDepartmentIdsOf(String departmentId) {
        return this.hierarchy.allChildIdsOf(departmentId);
    }

    public Map<String, String> departmentFullNames(Map<String, String> departmentNames) {
        return this.hierarchy.fullNames(departmentNames);
    }

    public Set<String> allDepartmentIds() {
        return this.hierarchy.allIds();
    }

    public Set<String> siblingDepartmentIdsOf(String departmentId) {
        return this.hierarchy.siblingIdsOf(departmentId);
    }

    public boolean containsDepartmentId(String departmentId) {
        return this.hierarchy.containsId(departmentId);
    }

    private void buildHierarchy() {
        try {
            this.hierarchy = this.idTree.buildHierarchy(MAX_GROUP_HIERARCHY_LEVEL);//深度与group保持相同，因为可能要同步到group
        } catch (IdNodeLevelOverflowException ex) {
            throw new MryException(DEPARTMENT_HIERARCHY_TOO_DEEP, "部门层级最多不能超过5层。", "tenantId", this.getTenantId());
        }
    }
}
