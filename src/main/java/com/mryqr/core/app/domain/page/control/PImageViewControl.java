package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.border.Border;
import com.mryqr.core.app.domain.ui.shadow.Shadow;
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

import static com.mryqr.common.exception.ErrorCode.IMAGE_ID_DUPLICATED;
import static com.mryqr.common.utils.Identified.isDuplicated;
import static com.mryqr.common.utils.MryConstants.*;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("IMAGE_VIEW_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PImageViewControl extends Control {
    public static final int MAX_IMAGE_SIZE = 5;

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_IMAGE_SIZE)
    private List<@Valid UploadedFile> images;//图片文件

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

    @Override
    public void doCorrect(AppSettingContext context) {
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        if (isDuplicated(images)) {
            throw new MryException(IMAGE_ID_DUPLICATED, "图片ID不能重复。");
        }
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

}
