package com.mryqr.core.register.domain;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.tenant.domain.CreateTenantResult;
import com.mryqr.core.tenant.domain.TenantFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.common.exception.ErrorCode.MEMBER_WITH_MOBILE_OR_EMAIL_ALREADY_EXISTS;
import static com.mryqr.common.utils.CommonUtils.isMobileNumber;
import static com.mryqr.common.utils.CommonUtils.maskMobileOrEmail;

@Component
@RequiredArgsConstructor
public class RegisterDomainService {
    private final MemberRepository memberRepository;
    private final TenantFactory tenantFactory;

    public CreateTenantResult register(String mobileOrEmail, String password, String tenantName, User user) {
        if (memberRepository.existsByMobileOrEmail(mobileOrEmail)) {
            throw new MryException(MEMBER_WITH_MOBILE_OR_EMAIL_ALREADY_EXISTS, "注册失败，手机号或邮箱已被占用。",
                    "mobileOrEmail", maskMobileOrEmail(mobileOrEmail));
        }

        String mobile = null;
        String email = null;
        if (isMobileNumber(mobileOrEmail)) {
            mobile = mobileOrEmail;
        } else {
            email = mobileOrEmail;
        }

        return tenantFactory.create(tenantName, mobile, email, password, user);
    }
}
