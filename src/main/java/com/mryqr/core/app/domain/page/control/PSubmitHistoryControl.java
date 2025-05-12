package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.collection.NoBlankString;
import com.mryqr.common.validation.collection.NoDuplicatedString;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.AppearanceStyle;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;

import static com.mryqr.common.exception.ErrorCode.VALIDATION_PAGE_NOT_EXIST;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@SuperBuilder
@TypeAlias("SUBMIT_HISTORY_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PSubmitHistoryControl extends Control {
    public static final int MAX_PAGE_SIZE = 10;

    public static final int MIN_MAX = 1;
    public static final int MAX_MAX = 100;

    @Valid
    @NotNull
    @NoBlankString
    @NoDuplicatedString
    @Size(max = MAX_PAGE_SIZE)
    private List<@PageId String> pageIds;//需要显示提交历史的页面

    @Valid
    @NotNull
    private AppearanceStyle appearanceStyle;//外观样式

    @Min(MIN_MAX)
    @Max(MAX_MAX)
    private int max; //最大显示条数

    private boolean showSubmitter;//显示提交者姓名

    private boolean showPageName;//显示页面提交动作名称

    private boolean orderByAsc;//按时间从早到晚顺序排列

    private boolean hideControlIfNoData;//无数据时隐藏整个控件（包括标题和描述）

    @Override
    public void doCorrect(AppSettingContext context) {
        this.complete = isNotEmpty(pageIds);
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        pageIds.forEach(pageId -> {
            if (context.pageNotExists(pageId)) {
                throw new MryException(VALIDATION_PAGE_NOT_EXIST, "提交历史引用页面不存在。", mapOf("pageId", pageId));
            }
        });
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

}
