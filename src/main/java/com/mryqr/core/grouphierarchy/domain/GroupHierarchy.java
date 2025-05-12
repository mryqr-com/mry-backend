package com.mryqr.core.grouphierarchy.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.AggregateRoot;
import com.mryqr.core.common.domain.idnode.IdTree;
import com.mryqr.core.common.domain.idnode.IdTreeHierarchy;
import com.mryqr.core.common.domain.idnode.exception.IdNodeLevelOverflowException;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
import java.util.Set;

import static com.mryqr.core.common.exception.ErrorCode.GROUP_HIERARCHY_TOO_DEEP;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_NOT_FOUND;
import static com.mryqr.core.common.utils.MryConstants.GROUP_HIERARCHY_COLLECTION;
import static com.mryqr.core.common.utils.MryConstants.MAX_GROUP_HIERARCHY_LEVEL;
import static com.mryqr.core.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Document(GROUP_HIERARCHY_COLLECTION)
@TypeAlias(GROUP_HIERARCHY_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class GroupHierarchy extends AggregateRoot {
    private String appId;
    private IdTree idTree;
    private IdTreeHierarchy hierarchy;

    public GroupHierarchy(App app, String initGroupId, User user) {
        super(newGroupHierarchyId(), app.getTenantId(), user);
        this.appId = app.getId();
        this.idTree = new IdTree(initGroupId);
        this.buildHierarchy();
        addOpsLog("新建", user);
    }

    public static String newGroupHierarchyId() {
        return "GHC" + newSnowflakeId();
    }

    public void update(IdTree idTree, User user) {
        this.idTree = idTree;
        this.buildHierarchy();
        addOpsLog("更新层级", user);
    }

    public void merge(IdTree anotherIdTree, User user) {
        this.idTree.merge(anotherIdTree);
        this.buildHierarchy();
        addOpsLog("合并", user);
    }

    public void addGroup(String parentGroupId, String groupId, User user) {
        if (isNotBlank(parentGroupId)) {
            if (!containsGroupId(parentGroupId)) {
                throw new MryException(GROUP_NOT_FOUND, "未找到分组。", "groupId", parentGroupId);
            }

            if (this.hierarchy.levelOf(parentGroupId) >= MAX_GROUP_HIERARCHY_LEVEL) {
                throw new MryException(GROUP_HIERARCHY_TOO_DEEP, "添加失败，分组层级最多不能超过5层。", "appId", this.appId);
            }
        }

        this.idTree.addNode(parentGroupId, groupId);
        this.buildHierarchy();
        addOpsLog("添加分组", user);
    }

    public void removeGroup(String groupId, User user) {
        this.idTree.removeNode(groupId);
        this.buildHierarchy();
        addOpsLog("删除分组", user);
    }

    public Set<String> directChildGroupIdsUnder(String parentGroupId) {
        return this.hierarchy.directChildIdsUnder(parentGroupId);
    }

    public Set<String> withAllSubGroupIdsOf(String groupId) {
        return this.hierarchy.withAllChildIdsOf(groupId);
    }

    public Set<String> allSubGroupIdsOf(String groupId) {
        return this.hierarchy.allChildIdsOf(groupId);
    }

    public Set<String> withAllParentGroupIdsOf(String groupId) {
        return this.hierarchy.withAllParentIdsOf(groupId);
    }

    public Set<String> allParentGroupIdsOf(String groupId) {
        return this.hierarchy.allParentIdsOf(groupId);
    }

    public Map<String, String> groupFullNames(Map<String, String> groupNames) {
        return this.hierarchy.fullNames(groupNames);
    }

    public Set<String> allGroupIds() {
        return this.hierarchy.allIds();
    }

    public Set<String> siblingGroupIdsOf(String groupId) {
        return this.hierarchy.siblingIdsOf(groupId);
    }

    public boolean containsGroupId(String groupId) {
        return this.hierarchy.containsId(groupId);
    }

    public int groupCount() {
        return this.hierarchy.allIds().size();
    }

    private void buildHierarchy() {
        try {
            this.hierarchy = this.idTree.buildHierarchy(MAX_GROUP_HIERARCHY_LEVEL);
        } catch (IdNodeLevelOverflowException ex) {
            throw new MryException(GROUP_HIERARCHY_TOO_DEEP, "分组层级最多不能超过5层。", "appId", this.appId);
        }
    }
}
