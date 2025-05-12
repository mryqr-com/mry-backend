package com.mryqr.core.verification.domain;

import com.mryqr.core.common.domain.AggregateRoot;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.mryqr.core.common.exception.ErrorCode.VERIFICATION_CODE_COUNT_OVERFLOW;
import static com.mryqr.core.common.utils.MryConstants.NO_TENANT_ID;
import static com.mryqr.core.common.utils.MryConstants.VERIFICATION_COLLECTION;
import static com.mryqr.core.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Document(VERIFICATION_COLLECTION)
@TypeAlias(VERIFICATION_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class VerificationCode extends AggregateRoot {
    private String mobileOrEmail;//邮箱或手机号
    private String code;//6位数验证码
    private VerificationCodeType type;//验证码用于的类型
    private int usedCount;//已经使用的次数，使用次数不能超过3次

    public VerificationCode(String mobileOrEmail, VerificationCodeType type, String tenantId, User user) {
        super(newVerificationCodeId(), isNotBlank(tenantId) ? tenantId : NO_TENANT_ID, user);
        this.mobileOrEmail = mobileOrEmail;
        this.code = randomNumeric(6);
        this.type = type;
        this.usedCount = 0;
    }

    public static String newVerificationCodeId() {
        return "VRC" + newSnowflakeId();
    }

    public void use() {
        if (usedCount >= 3) {
            throw new MryException(VERIFICATION_CODE_COUNT_OVERFLOW, "验证码已超过可使用次数。");
        }

        this.usedCount++;
    }

}
