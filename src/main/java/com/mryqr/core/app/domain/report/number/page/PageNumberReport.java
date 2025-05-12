package com.mryqr.core.app.domain.report.number.page;

import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.page.PageAware;
import com.mryqr.core.app.domain.report.number.NumberReport;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.common.exception.ErrorCode.VALIDATION_PAGE_NOT_EXIST;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("PAGE_NUMBER_REPORT")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PageNumberReport extends NumberReport implements PageAware {

    @PageId
    @NotNull
    private String pageId;

    @NotNull
    private PageNumberReportType pageNumberReportType;

    @Override
    public void validate(AppSettingContext context) {
        if (context.pageNotExists(pageId)) {
            throw new MryException(VALIDATION_PAGE_NOT_EXIST, "报表所引用的页面不存在。",
                    mapOf("reportId", this.getId(), "refPageId", pageId));
        }
    }

    @Override
    public Set<String> awaredPageIds() {
        return isNotBlank(this.pageId) ? Set.of(this.pageId) : Set.of();
    }
}
