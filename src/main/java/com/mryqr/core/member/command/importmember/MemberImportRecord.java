package com.mryqr.core.member.command.importmember;

import com.alibaba.excel.annotation.ExcelProperty;
import com.mryqr.core.common.validation.email.Email;
import com.mryqr.core.common.validation.id.custom.CustomId;
import com.mryqr.core.common.validation.mobile.Mobile;
import com.mryqr.core.common.validation.password.Password;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Data
public class MemberImportRecord {
    public static final String NAME_FILED_NAME = "姓名（必填）";
    public static final String MOBILE_FIELD_NAME = "手机号（手机号和邮箱不能同时为空）";
    public static final String EMAIL_FIELD_NAME = "邮箱（邮箱和手机号不能同时为空）";
    public static final String PASSWORD_FIELD_NAME = "初始密码（必填）";
    public static final String CUSTOM_ID_FIELD_NAME = "自定义编号（可空）";

    @NotBlank(message = "姓名不能为空")
    @Size(max = MAX_GENERIC_NAME_LENGTH, message = "姓名长度超出最大限制")
    @ExcelProperty(NAME_FILED_NAME)
    private String name;

    @Mobile(message = "手机号格式错误")
    @ExcelProperty(MOBILE_FIELD_NAME)
    private String mobile;

    @Email(message = "邮箱格式错误")
    @ExcelProperty(EMAIL_FIELD_NAME)
    private String email;

    @NotBlank(message = "初始密码不能为空")
    @Password(message = "初始密码格式错误")
    @ExcelProperty(PASSWORD_FIELD_NAME)
    private String password;

    @CustomId(message = "自定义编号格式错误")
    @ExcelProperty(CUSTOM_ID_FIELD_NAME)
    private String customId;

    private int rowIndex;

    private List<String> errors;

    public void addError(String errorMessage) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(errorMessage);
    }

    public boolean hasError() {
        return isNotEmpty(errors);
    }
}
