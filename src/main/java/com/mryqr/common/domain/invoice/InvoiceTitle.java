package com.mryqr.common.domain.invoice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.common.utils.MryRegexConstants.*;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class InvoiceTitle {
    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Pattern(regexp = UNIFIED_CODE_PATTERN, message = "统一社会信用代码格式错误")
    private String unifiedCode;

    @NotBlank
    @Size(max = 100)
    private String bankName;

    @NotBlank
    @Pattern(regexp = BANK_ACCOUNT_PATTERN, message = "银行账号格式错误")
    private String bankAccount;

    @NotBlank
    @Size(max = 100)
    private String address;

    @NotBlank
    @Pattern(regexp = PHONE_PATTERN, message = "电话号码格式错误")
    private String phone;
}
