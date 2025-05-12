package com.mryqr.management.crm.webhook;

import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionCreatedWebhookPayload;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.department.domain.DepartmentRepository;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchyRepository;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.checkbox.CheckboxAnswer;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.mryqr.management.crm.MryTenantManageApp.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClearTenantCacheWebhookHandler implements TenantWebhookHandler {
    private final AppRepository appRepository;
    private final GroupRepository groupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final TenantRepository tenantRepository;
    private final TaskExecutor taskExecutor;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload instanceof SubmissionCreatedWebhookPayload && payload.getPageId().equals(CLEAR_CACHE_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String tenantId = payload.getQrCustomId();
        Map<String, Answer> answers = payload.allAnswers();

        Answer answer = answers.get(CLEAR_CACHE_CONTROL_ID);
        if (answer == null) {
            return;
        }

        taskExecutor.execute(() -> doClearCache(tenantId, ((CheckboxAnswer) answer).getOptionIds()));
    }

    private void doClearCache(String tenantId, List<String> optionIds) {
        //清空App相关缓存
        if (optionIds.contains(CLEAR_APP_CACHE_OPTION_ID)) {
            MryTaskRunner.run(() -> appRepository.evictTenantAppsCache(tenantId));
            appRepository.allAppIdsOf(tenantId).forEach(appId -> MryTaskRunner.run(() -> appRepository.evictAppCache(appId)));
        }

        //清空Group相关缓存
        if (optionIds.contains(CLEAR_GROUP_CACHE_OPTION_ID)) {
            appRepository.allAppIdsOf(tenantId).forEach(appId -> {
                MryTaskRunner.run(() -> groupRepository.evictAppGroupsCache(appId));
                MryTaskRunner.run(() -> groupHierarchyRepository.evictGroupHierarchyCache(appId));
                groupRepository.allGroupIdsOf(appId).forEach(groupId -> MryTaskRunner.run(() -> groupRepository.evictGroupCache(groupId)));
            });
        }

        //清空Member相关缓存
        if (optionIds.contains(CLEAR_MEMBER_CACHE_OPTION_ID)) {
            MryTaskRunner.run(() -> memberRepository.evictTenantMembersCache(tenantId));
            memberRepository.allMemberIdsOf(tenantId).forEach(memberId -> MryTaskRunner.run(() -> memberRepository.evictMemberCache(memberId)));
        }

        //清空Department相关缓存
        if (optionIds.contains(CLEAR_DEPARTMENT_CACHE_OPTION_ID)) {
            MryTaskRunner.run(() -> departmentRepository.evictTenantDepartmentsCache(tenantId));
            MryTaskRunner.run(() -> departmentHierarchyRepository.evictDepartmentHierarchyCache(tenantId));
        }

        //清空Tenant相关缓存
        if (optionIds.contains(CLEAR_TENANT_CACHE_OPTION_ID)) {
            MryTaskRunner.run(() -> tenantRepository.evictTenantCache(tenantId));
            tenantRepository.byIdOptional(tenantId)
                    .ifPresent(tenant -> MryTaskRunner.run(() -> tenantRepository.evictTenantCacheByApiKey(tenant.getApiSetting().getApiKey())));
        }
    }
}
