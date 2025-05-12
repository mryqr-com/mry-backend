package com.mryqr.common.oss.command;


import com.mryqr.common.exception.MryException;
import com.mryqr.common.oss.domain.OssTokenRequestType;
import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.id.app.AppId;
import com.mryqr.common.validation.id.member.MemberId;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.common.validation.id.qr.QrId;
import com.mryqr.common.validation.id.tenant.TenantId;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.NoSuchElementException;

import static com.mryqr.common.exception.MryException.requestValidationException;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class RequestOssTokenCommand implements Command {

    @TenantId
    private final String tenantId;

    @AppId
    private final String appId;

    @QrId
    private final String qrId;

    @PageId
    private final String pageId;

    @MemberId
    private final String memberId;

    @NotNull
    private final OssTokenRequestType type;

    @Override
    public void correctAndValidate() {
        switch (type) {
            case TENANT_EDIT, TENANT_ORDER -> {
                if (isBlank(tenantId)) {
                    throw throwNotValidRequestException();
                }
            }
            case APP_EDIT -> {
                if (isBlank(tenantId) || isBlank(appId)) {
                    throw throwNotValidRequestException();
                }
            }
            case QR_MANAGE -> {
                if (isBlank(tenantId) || isBlank(appId) || isBlank(qrId)) {
                    throw throwNotValidRequestException();
                }
            }
            case SUBMISSION -> {
                if (isBlank(tenantId) || isBlank(appId) || isBlank(qrId) || isBlank(pageId)) {
                    throw throwNotValidRequestException();
                }
            }
            case MEMBER_INFO -> {
                if (isBlank(tenantId) || isBlank(memberId)) {
                    throw throwNotValidRequestException();
                }
            }
            default -> {
                throw throwNotValidRequestException();
            }
        }
    }

    private MryException throwNotValidRequestException() {
        return requestValidationException("ossRequestType", type);
    }

    public String folder() {
        switch (type) {
            case TENANT_EDIT -> {
                return tenantId + "/_TENANT_EDIT";
            }
            case TENANT_ORDER -> {
                return tenantId + "/_TENANT_ORDER";
            }
            case APP_EDIT -> {
                return tenantId + "/" + appId + "/_APP_EDIT";
            }
            case QR_MANAGE -> {
                return tenantId + "/" + appId + "/" + qrId + "/_QR_MANAGE";
            }
            case SUBMISSION -> {
                return tenantId + "/" + appId + "/" + qrId + "/" + pageId + "/_SUBMISSION";
            }
            case MEMBER_INFO -> {
                return tenantId + "/_MEMBER_INFO/" + memberId;
            }
            default -> {
                throw new NoSuchElementException("Oss token type[" + type.name() + "] not supported.");
            }
        }
    }

}
