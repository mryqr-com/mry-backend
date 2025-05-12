package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.AppearanceStyle;
import com.mryqr.core.app.domain.ui.BoxedTextStyle;
import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.collection.NoNullElement;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;

import static com.mryqr.core.common.exception.ErrorCode.ATTACHMENT_ID_DUPLICATED;
import static com.mryqr.core.common.utils.Identified.isDuplicated;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("ATTACHMENT_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PAttachmentViewControl extends Control {
    public static final int MAX_ATTACHMENT_SIZE = 5;

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_ATTACHMENT_SIZE)
    private List<@Valid UploadedFile> attachments;//附件文件

    @Valid
    @NotNull
    private BoxedTextStyle fileNameStyle;//文件名样式

    @Valid
    @NotNull
    private AppearanceStyle appearanceStyle;//外观样式

    @Override
    public void doCorrect(AppSettingContext context) {
        attachments.forEach(UploadedFile::correct);
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        if (isDuplicated(attachments)) {
            throw new MryException(ATTACHMENT_ID_DUPLICATED, "附件ID不能重复。");
        }
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

}
