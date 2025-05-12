package com.mryqr.core.verification.command;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.verification.domain.VerificationCode;
import com.mryqr.core.verification.domain.VerificationCodeFactory;
import com.mryqr.core.verification.domain.VerificationCodeRepository;
import com.mryqr.core.verification.domain.VerificationCodeSender;
import com.mryqr.core.verification.domain.VerificationCodeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.core.common.utils.CommonUtils.maskMobileOrEmail;
import static com.mryqr.core.verification.domain.VerificationCode.newVerificationCodeId;
import static com.mryqr.core.verification.domain.VerificationCodeType.CHANGE_MOBILE;
import static com.mryqr.core.verification.domain.VerificationCodeType.FINDBACK_PASSWORD;
import static com.mryqr.core.verification.domain.VerificationCodeType.IDENTIFY_MOBILE;
import static com.mryqr.core.verification.domain.VerificationCodeType.LOGIN;
import static com.mryqr.core.verification.domain.VerificationCodeType.REGISTER;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationCodeCommandService {
    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationCodeFactory verificationCodeFactory;
    private final VerificationCodeSender verificationCodeSender;
    private final MemberRepository memberRepository;
    private final MryRateLimiter mryRateLimiter;

    @Transactional
    public String createVerificationCodeForRegister(CreateRegisterVerificationCodeCommand command) {
        String mobileOrEmail = command.getMobileOrEmail();
        mryRateLimiter.applyFor("VerificationCode:Register:All", 20);
        mryRateLimiter.applyFor("VerificationCode:Register:" + mobileOrEmail, 1);

        if (memberRepository.existsByMobileOrEmail(mobileOrEmail)) {
            log.warn("[{}] already exists for register.", maskMobileOrEmail(mobileOrEmail));
            return newVerificationCodeId();
        }

        String verificationCodeId = createVerificationCode(mobileOrEmail, REGISTER, null, NOUSER);
        log.info("Created verification code[{}] for register for [{}].", verificationCodeId, maskMobileOrEmail(command.getMobileOrEmail()));
        return verificationCodeId;
    }

    @Transactional
    public String createVerificationCodeForLogin(CreateLoginVerificationCodeCommand command) {
        String mobileOrEmail = command.getMobileOrEmail();
        mryRateLimiter.applyFor("VerificationCode:Login:All", 100);
        mryRateLimiter.applyFor("VerificationCode:Login:" + mobileOrEmail, 1);

        String verificationCodeId = memberRepository.byMobileOrEmailOptional(mobileOrEmail)
                .map(member -> createVerificationCode(mobileOrEmail, LOGIN, member.getTenantId(), NOUSER))
                .orElseGet(() -> {
                    log.warn("No user exists for [{}] for login.", maskMobileOrEmail(mobileOrEmail));
                    return newVerificationCodeId();
                });

        log.info("Created verification code[{}] for login for [{}].", verificationCodeId, maskMobileOrEmail(command.getMobileOrEmail()));
        return verificationCodeId;
    }

    @Transactional
    public String createVerificationCodeForFindbackPassword(CreateFindbackPasswordVerificationCodeCommand command) {
        String mobileOrEmail = command.getMobileOrEmail();
        mryRateLimiter.applyFor("VerificationCode:FindbackPassword:All", 10);
        mryRateLimiter.applyFor("VerificationCode:FindbackPassword:" + mobileOrEmail, 1);

        String verificationCodeId = memberRepository.byMobileOrEmailOptional(mobileOrEmail)
                .map(member -> createVerificationCode(mobileOrEmail, FINDBACK_PASSWORD, member.getTenantId(), NOUSER))
                .orElseGet(() -> {
                    log.warn("No user exists for [{}] for findback password.", mobileOrEmail);
                    return newVerificationCodeId();
                });

        log.info("Created verification code[{}] for find back password for [{}].",
                verificationCodeId, maskMobileOrEmail(command.getMobileOrEmail()));
        return verificationCodeId;
    }

    @Transactional
    public String createVerificationCodeForChangeMobile(CreateChangeMobileVerificationCodeCommand command, User user) {
        String mobile = command.getMobile();
        mryRateLimiter.applyFor("VerificationCode:ChangeMobile:All", 10);
        mryRateLimiter.applyFor("VerificationCode:ChangeMobile:" + mobile, 1);

        if (memberRepository.existsByMobile(mobile)) {
            log.warn("Mobile [{}] already exists for change mobile.", maskMobileOrEmail(mobile));
            return newVerificationCodeId();
        }

        String verificationCodeId = createVerificationCode(mobile, CHANGE_MOBILE, user.getTenantId(), user);
        log.info("Created verification code[{}] for change mobile for [{}].", verificationCodeId, maskMobileOrEmail(command.getMobile()));

        return verificationCodeId;
    }

    @Transactional
    public String createVerificationCodeForIdentifyMobile(IdentifyMobileVerificationCodeCommand command, User user) {
        mryRateLimiter.applyFor("VerificationCode:IdentifyMobile:All", 20);
        mryRateLimiter.applyFor("VerificationCode:IdentifyMobile:" + command.getMobile(), 1);

        String verificationCodeId = createVerificationCode(command.getMobile(), IDENTIFY_MOBILE, user.getTenantId(), user);
        log.info("Created verification code[{}] for identify mobile for [{}].", verificationCodeId, command.getMobile());
        return verificationCodeId;
    }

    private String createVerificationCode(String mobileOrEmail, VerificationCodeType type, String tenantId, User user) {
        Optional<VerificationCode> verificationCodeOptional = verificationCodeFactory.create(mobileOrEmail, type, tenantId, user);
        if (verificationCodeOptional.isPresent()) {
            VerificationCode code = verificationCodeOptional.get();
            verificationCodeRepository.save(code);
            verificationCodeSender.send(code);
            return code.getId();
        } else {
            return newVerificationCodeId();
        }
    }
}
