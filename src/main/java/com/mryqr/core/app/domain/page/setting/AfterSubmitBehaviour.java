package com.mryqr.core.app.domain.page.setting;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.common.validation.id.page.PageId;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.mryqr.core.app.domain.page.setting.AfterSubmitNavigationType.DEFAULT;
import static com.mryqr.core.common.utils.CommonUtils.isValidUrl;
import static com.mryqr.core.common.utils.MryConstants.MAX_URL_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class AfterSubmitBehaviour {
    @NotNull
    private AfterSubmitNavigationType type;//提交后导向页面类型

    @PageId
    private String internalPageId;

    @Size(max = MAX_URL_LENGTH)
    private String externalUrl;

    private boolean enableMessage;

    @Size(max = 100)
    private String message;

    public void correct(AppSettingContext context) {
        switch (type) {
            case DEFAULT -> {
                internalPageId = null;
                externalUrl = null;
            }
            case INTERNAL_PAGE -> {
                externalUrl = null;
            }
            case EXTERNAL_URL -> {
                internalPageId = null;
            }
        }

        if (isBlank(externalUrl) && isBlank(internalPageId)) {
            type = DEFAULT;
        }

        if (isNotBlank(internalPageId) && context.pageNotExists(internalPageId)) {
            internalPageId = null;
            type = DEFAULT;
        }

        if (isNotBlank(externalUrl) && !isValidUrl(externalUrl)) {
            externalUrl = null;
            type = DEFAULT;
        }

        if (this.type == DEFAULT) {
            this.enableMessage = false;
        }

        if (!this.enableMessage || isBlank(this.message)) {
            message = "提交成功！";
        }
    }

    public void reset() {
        type = DEFAULT;
        internalPageId = null;
        externalUrl = null;
    }

}
