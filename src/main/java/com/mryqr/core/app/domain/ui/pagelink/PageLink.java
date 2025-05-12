package com.mryqr.core.app.domain.ui.pagelink;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.utils.Identified;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.app.domain.AppSettingContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.mryqr.common.exception.ErrorCode.VALIDATION_LINK_PAGE_NOT_EXIST;
import static com.mryqr.common.utils.CommonUtils.isValidUrl;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static com.mryqr.common.utils.MryConstants.MAX_URL_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class PageLink implements Identified {
    @NotBlank
    @ShortUuid
    private final String id;//id，用于前端显示时作为key

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String name;//名称

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String buttonText;

    @Size(max = 100)
    private final String description;//简介

    private final PageLinkType type;//链接类型

    @Size(max = MAX_URL_LENGTH)
    private final String url;//当为外部链接时的url

    @PageId
    private final String pageId;//当为内部链接时的页面id

    @Valid
    private final UploadedFile image;//链接图标或图片

    @EqualsAndHashCode.Exclude
    private boolean complete;//是否完整，计算值

    public void correct() {
        if (isBlank(name)) {
            this.name = "未命名";
        }

        if (isBlank(buttonText)) {
            this.buttonText = "未命名";
        }

        this.complete = deriveComplete();
    }

    private boolean deriveComplete() {
        if (type == null) {
            return false;
        }

        return type == PageLinkType.PAGE ? isNotBlank(pageId) : isValidUrl(url);
    }

    public void validate(AppSettingContext context) {
        if (complete && type == PageLinkType.PAGE && context.pageNotExists(pageId)) {
            throw new MryException(VALIDATION_LINK_PAGE_NOT_EXIST, "链接对应的页面不存在。",
                    mapOf("pageLinkId", id, "linkPageId", pageId));
        }
    }

    @Override
    public String getIdentifier() {
        return id;
    }
}
