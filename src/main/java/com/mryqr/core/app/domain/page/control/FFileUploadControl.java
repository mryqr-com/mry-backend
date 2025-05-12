package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.ButtonStyle;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.fileupload.FileUploadAnswer;
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

import static com.mryqr.core.common.exception.ErrorCode.MAX_FILE_NUMBER_REACHED;
import static com.mryqr.core.common.utils.MryConstants.MAX_SHORT_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;


@Getter
@SuperBuilder
@TypeAlias("FILE_UPLOAD_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FFileUploadControl extends Control {
    public static final int MIN_MAX_FILE_SIZE = 1;
    public static final int MAX_MAX_FILE_SIZE = 10;

    private boolean nameEditable;//名称是否可编辑
    private boolean sortable;//多个文件时是否可排序

    @Min(MIN_MAX_FILE_SIZE)
    @Max(MAX_MAX_FILE_SIZE)
    private int max;//最大允许上传文件数量

    @Min(1)
    @Max(200)
    private int perMaxSize;//每个文件最大尺寸，以MB为单位

    @NotNull
    private FileCategory category;//文件类型

    @Size(max = MAX_SHORT_NAME_LENGTH)
    private String buttonText;//按钮文本

    @Valid
    @NotNull
    private ButtonStyle buttonStyle;//按钮样式

    @Override
    public void doCorrect(AppSettingContext context) {
        if (isBlank(buttonText)) {
            this.buttonText = "添加文件";
        }
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

    public FileUploadAnswer check(FileUploadAnswer answer) {
        if (answer.getFiles().size() > max) {
            failAnswerValidation(MAX_FILE_NUMBER_REACHED, "上传文件数超过限制。");
        }

        return answer;
    }

}
