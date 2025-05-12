package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.common.validation.collection.NoNullElement;
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

import static com.mryqr.core.common.utils.MryConstants.MAX_BORDER_RADIUS;
import static com.mryqr.core.common.utils.MryConstants.MIN_BORDER_RADIUS;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("VIDEO_VIEW_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PVideoViewControl extends Control {
    public static final int MAX_VIDEO_SIZE = 1;

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_VIDEO_SIZE)
    private List<@Valid UploadedFile> videos;//视频文件

    @Valid
    private UploadedFile poster;//预览图

    @Min(MIN_BORDER_RADIUS)
    @Max(MAX_BORDER_RADIUS)
    private int borderRadius;//圆角半径

    @Override
    public void doCorrect(AppSettingContext context) {
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

}
