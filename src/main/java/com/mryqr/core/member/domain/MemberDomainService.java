package com.mryqr.core.member.domain;

import com.mryqr.common.password.MryPasswordEncoder;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.department.domain.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.mryqr.core.common.exception.ErrorCode.MAX_TENANT_ADMIN_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.MEMBER_WITH_CUSTOM_ID_ALREADY_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.MEMBER_WITH_EMAIL_ALREADY_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.MEMBER_WITH_MOBILE_ALREADY_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.MOBILE_EMAIL_CANNOT_BOTH_EMPTY;
import static com.mryqr.core.common.exception.ErrorCode.NEW_PASSWORD_SAME_WITH_OLD;
import static com.mryqr.core.common.exception.ErrorCode.NOT_ALL_DEPARTMENTS_EXITS;
import static com.mryqr.core.common.exception.ErrorCode.NO_ACTIVE_TENANT_ADMIN_LEFT;
import static com.mryqr.core.common.exception.ErrorCode.PASSWORD_NOT_MATCH;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Component
@RequiredArgsConstructor
public class MemberDomainService {
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final MryPasswordEncoder mryPasswordEncoder;

    public void updateMember(Member member, String name, List<String> departmentIds, String mobile, String email, User user) {
        if (departmentRepository.cachedNotAllDepartmentsExist(departmentIds, member.getTenantId())) {
            throw new MryException(NOT_ALL_DEPARTMENTS_EXITS, "更新成员失败，有部门不存在。", "name", name, "departmentIds", departmentIds);
        }

        if (isBlank(mobile) && isBlank(email)) {
            throw new MryException(MOBILE_EMAIL_CANNOT_BOTH_EMPTY, "更新成员失败，手机号和邮箱不能同时为空。",
                    "memberId", member.getId());
        }

        if (isNotBlank(mobile) && !mobile.equals(member.getMobile()) && memberRepository.existsByMobile(mobile)) {
            throw new MryException(MEMBER_WITH_MOBILE_ALREADY_EXISTS, "更新成员失败，手机号已被占用。",
                    mapOf("memberId", member.getId()));
        }

        if (isNotBlank(email) && !email.equals(member.getEmail()) && memberRepository.existsByEmail(email)) {
            throw new MryException(MEMBER_WITH_EMAIL_ALREADY_EXISTS, "更新成员失败，邮箱已被占用。",
                    mapOf("memberId", member.getId()));
        }

        member.update(name, departmentIds, mobile, email, user);
    }

    public void checkMinTenantAdminLimit(String tenantId) {
        int count = memberRepository.cachedActiveTenantAdminCountFor(tenantId);
        if (count < 1) {
            throw new MryException(NO_ACTIVE_TENANT_ADMIN_LEFT, "必须保留至少一个可用的系统管理员。",
                    mapOf("tenantId", tenantId));
        }
    }

    public void checkMaxTenantAdminLimit(String tenantId) {
        int count = memberRepository.cachedTenantAdminCountFor(tenantId);
        if (count > 10) {
            throw new MryException(MAX_TENANT_ADMIN_REACHED, "系统管理员数量已超出最大限制（10名）。",
                    mapOf("tenantId", tenantId));
        }
    }

    public void changeMyPassword(Member member, String oldPassword, String newPassword) {
        if (Objects.equals(oldPassword, newPassword)) {
            throw new MryException(NEW_PASSWORD_SAME_WITH_OLD, "修改密码失败，新密码不能与原密码相同。", "memberId", member.getId());
        }

        if (!mryPasswordEncoder.matches(oldPassword, member.getPassword())) {
            throw new MryException(PASSWORD_NOT_MATCH, "修改密码失败，原密码不正确。", "memberId", member.getId());
        }

        member.changePassword(mryPasswordEncoder.encode(newPassword), member.toUser());
    }

    public void changeMyMobile(Member member, String newMobile, String password) {
        if (!mryPasswordEncoder.matches(password, member.getPassword())) {
            throw new MryException(PASSWORD_NOT_MATCH, "修改手机号失败，密码不正确。", "memberId", member.getId());
        }

        if (Objects.equals(member.getMobile(), newMobile)) {
            return;
        }

        if (memberRepository.existsByMobile(newMobile)) {
            throw new MryException(MEMBER_WITH_MOBILE_ALREADY_EXISTS, "修改手机号失败，手机号对应成员已存在。",
                    mapOf("mobile", newMobile, "memberId", member.getId()));
        }

        member.changeMobile(newMobile, member.toUser());
    }

    public void identifyMyMobile(Member member, String mobile) {
        if (!Objects.equals(member.getMobile(), mobile) && memberRepository.existsByMobile(mobile)) {
            throw new MryException(MEMBER_WITH_MOBILE_ALREADY_EXISTS, "认证失败，手机号对应成员已存在。",
                    mapOf("mobile", mobile, "memberId", member.getId(), "mobile", mobile));
        }

        member.identifyMobile(mobile, member.toUser());
    }

    public void updateCustomId(Member member, String customId, User user) {
        checkCustomIdDuplication(member, customId);
        member.updateCustomId(customId, user);
    }

    private void checkCustomIdDuplication(Member member, String customId) {
        if (isNotBlank(customId)
                && !Objects.equals(member.getCustomId(), customId)
                && memberRepository.cachedExistsByCustomId(customId, member.getTenantId())) {
            throw new MryException(MEMBER_WITH_CUSTOM_ID_ALREADY_EXISTS,
                    "自定义编号已被占用。",
                    mapOf("qrId", member.getId(), "tenantId", member.getTenantId()));
        }
    }

    @Transactional(propagation = REQUIRES_NEW)//使用REQUIRES_NEW保证即便其他地方有异常，这里也能正常写库
    public void recordMemberFailedLogin(Member member) {
        member.recordFailedLogin();
        memberRepository.save(member);
    }
}
