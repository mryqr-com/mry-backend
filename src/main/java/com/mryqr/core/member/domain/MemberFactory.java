package com.mryqr.core.member.domain;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.core.department.domain.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class MemberFactory {
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;

    public Member create(String name,
                         List<String> departmentIds,
                         String mobile,
                         String email,
                         String password,
                         User user) {
        return create(name, departmentIds, mobile, email, password, null, user);
    }

    public Member create(String name,
                         List<String> departmentIds,
                         String mobile,
                         String email,
                         String password,
                         String customId,
                         User user) {
        String tenantId = user.getTenantId();
        if (departmentRepository.cachedNotAllDepartmentsExist(departmentIds, tenantId)) {
            throw new MryException(NOT_ALL_DEPARTMENTS_EXITS, "添加成员失败，有部门不存在。", "name", name, "departmentIds", departmentIds);
        }

        if (isBlank(mobile) && isBlank(email)) {
            throw new MryException(MOBILE_EMAIL_CANNOT_BOTH_EMPTY, "添加成员失败，手机号和邮箱不能同时为空。", "tenantId", tenantId);
        }

        if (isNotBlank(mobile) && memberRepository.existsByMobile(mobile)) {
            throw new MryException(MEMBER_WITH_MOBILE_ALREADY_EXISTS, "添加成员失败，手机号已被占用。", mapOf("mobile", mobile));
        }

        if (isNotBlank(email) && memberRepository.existsByEmail(email)) {
            throw new MryException(MEMBER_WITH_EMAIL_ALREADY_EXISTS, "添加成员失败，邮箱已被占用。", mapOf("email", email));
        }

        if (isNotBlank(customId) && memberRepository.cachedExistsByCustomId(customId, tenantId)) {
            throw new MryException(MEMBER_WITH_CUSTOM_ID_ALREADY_EXISTS, "添加成员失败，自定义编号已被占用。", mapOf("customId", customId));
        }

        return new Member(name, departmentIds, mobile, email, password, customId, user);
    }

}
