package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.ButtonStyle;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.imageupload.ImageUploadAnswer;
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

import static com.mryqr.core.common.exception.ErrorCode.MAX_IMAGE_NUMBER_REACHED;
import static com.mryqr.core.common.utils.MryConstants.MAX_SHORT_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;


@Getter
@SuperBuilder
@TypeAlias("IMAGE_UPLOAD_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FImageUploadControl extends Control {
    public static final int MIN_MAX_IMAGE_SIZE = 1;
    public static final int MAX_MAX_IMAGE_SIZE = 10;

    private boolean nameEditable;//名称是否可编辑
    private boolean sortable;//多个图片时是否可以排序
    private boolean onlyOnSite;//只允许现场拍照

    @NotNull
    private FileCompressType compressType;//压缩类型

    @Min(MIN_MAX_IMAGE_SIZE)
    @Max(MAX_MAX_IMAGE_SIZE)
    private int max;//最大上传图片数量

    @Size(max = MAX_SHORT_NAME_LENGTH)
    private String buttonText;//上传按钮文本

    @Valid
    @NotNull
    private ButtonStyle buttonStyle;//按钮样式

    @Override
    public void doCorrect(AppSettingContext context) {
        if (isBlank(buttonText)) {
            this.buttonText = "添加图片";
        }
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

    public ImageUploadAnswer check(ImageUploadAnswer answer) {
        if (answer.getImages().size() > max) {
            failAnswerValidation(MAX_IMAGE_NUMBER_REACHED, "上传图片数量超过最大限制。");
        }

        return answer;
    }

}
