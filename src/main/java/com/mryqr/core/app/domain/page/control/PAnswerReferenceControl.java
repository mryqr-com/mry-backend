package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.id.control.ControlId;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.AppearanceStyle;
import com.mryqr.core.app.domain.ui.BoxedTextStyle;
import com.mryqr.core.app.domain.ui.MarkdownStyle;
import com.mryqr.core.app.domain.ui.border.Border;
import com.mryqr.core.app.domain.ui.shadow.Shadow;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.MAX_MARGIN;
import static com.mryqr.common.utils.MryConstants.MIN_MARGIN;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("ANSWER_REFERENCE_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PAnswerReferenceControl extends Control {
    @PageId
    private String pageId;//所引用的页面ID

    @ControlId
    private String controlId;//所引用的空间ID

    private boolean hideControlIfNoData;//无数据时隐藏整个控件（包括标题和描述）

    @Valid
    @NotNull
    private TextAnswerStyle textAnswerStyle;//引用为文本时的样式

    @Valid
    @NotNull
    private MarkdownAnswerStyle markdownAnswerStyle;//引用为markdown时的样式

    @Valid
    @NotNull
    private ImageAnswerStyle imageAnswerStyle;//引用为图片时的样式

    @Valid
    @NotNull
    private FileAnswerStyle fileAnswerStyle;//引用为文件列表时的样式

    @Valid
    @NotNull
    private VideoAnswerStyle videoAnswerStyle;//引用为视频列表时的样式

    @Valid
    @NotNull
    private AudioAnswerStyle audioAnswerStyle;//引用为音频列表时的样式

    @Override
    public void doCorrect(AppSettingContext context) {
        this.complete = isNotBlank(pageId) && isNotBlank(controlId);
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        if (!complete) {
            return;
        }

        if (context.pageNotExists(pageId)) {
            throw new MryException(VALIDATION_PAGE_NOT_EXIST, "答案引用的页面不存在。",
                    mapOf("refPageId", pageId));
        }

        if (context.controlNotExists(pageId, controlId)) {
            throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "答案引用的控件不存在。",
                    mapOf("refPageId", pageId, "refControlId", controlId));
        }

        if (!context.controlTypeOf(controlId).isFillable()) {
            throw new MryException(CONTROL_NOT_SUPPORT_REFERENCE, "控件不支持答案引用。",
                    mapOf("refPageId", pageId, "refControlId", controlId));
        }
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class TextAnswerStyle {
        @Valid
        @NotNull
        private BoxedTextStyle textStyle;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class MarkdownAnswerStyle {
        @Valid
        @NotNull
        private MarkdownStyle markdownStyle;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class ImageAnswerStyle {
        @Min(20)
        @Max(100)
        private int widthRatio;//图片宽度比

        private boolean showImageName;//是否显示图片名称

        @Min(MIN_MARGIN)
        @Max(MAX_MARGIN)
        private int verticalMargin;//图片垂直间距

        @Valid
        @NotNull
        private Border border;//边框

        @Valid
        @NotNull
        private Shadow shadow;//图片阴影
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class FileAnswerStyle {
        @Valid
        @NotNull
        private BoxedTextStyle fileNameStyle;

        @Valid
        @NotNull
        private AppearanceStyle appearanceStyle;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class VideoAnswerStyle {
        private boolean showFileName;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class AudioAnswerStyle {
        private boolean showFileName;
    }
}
