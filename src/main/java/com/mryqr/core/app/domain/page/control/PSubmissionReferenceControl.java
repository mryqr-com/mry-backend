package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.AppearanceStyle;
import com.mryqr.core.app.domain.ui.BoxedTextStyle;
import com.mryqr.core.app.domain.ui.FontStyle;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.id.page.PageId;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_PAGE_NOT_EXIST;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("SUBMISSION_REFERENCE_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PSubmissionReferenceControl extends Control {
    @PageId
    private String pageId;//所引用的页面ID

    private boolean stripped;//表格是否条纹显示

    private boolean hideControlIfNoData;//无数据时隐藏整个控件（包括标题和描述）

    @NotNull
    private StyleType styleType;//显示样式

    @Valid
    @NotNull
    private FontStyle keyFontStyle;//横向时字段名称样式

    @Valid
    @NotNull
    private FontStyle valueFontStyle;//横向时字段值样式

    private boolean showHeader;//是否显示表头

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String headerText;//表头文本

    @Valid
    @NotNull
    private FontStyle headerFontStyle;//横向时表头样式

    @Valid
    @NotNull
    private BoxedTextStyle verticalKeyStyle;//竖向时字段名称样式

    @Valid
    @NotNull
    private BoxedTextStyle verticalValueStyle;//竖向时字段值样式

    @Valid
    @NotNull
    private AppearanceStyle appearanceStyle;//竖向时的外观样式

    @Override
    public void doCorrect(AppSettingContext context) {
        if (!this.showHeader || isBlank(this.headerText)) {
            this.headerText = "未命名表头";
        }

        this.complete = isNotBlank(pageId);
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        if (!complete) {
            return;
        }

        if (context.pageNotExists(pageId)) {
            throw new MryException(VALIDATION_PAGE_NOT_EXIST, "表单引用控件所引用的页面不存在。", mapOf("pageId", pageId));
        }
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

    public enum StyleType {
        HORIZONTAL_TABLE,
        VERTICAL_TABLE,
    }
}
