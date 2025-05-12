package com.mryqr.core.submission.domain.answer.fileupload;


import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FFileUploadControl;
import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.FilesDisplayValue;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.collection.NoNullElement;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.FilesAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
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
import java.util.Set;

import static com.mryqr.core.app.domain.page.control.FFileUploadControl.MAX_MAX_FILE_SIZE;
import static com.mryqr.core.common.exception.ErrorCode.UPLOAD_FILE_ID_DUPLICATED;
import static com.mryqr.core.common.utils.Identified.isDuplicated;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@SuperBuilder
@TypeAlias("FILE_UPLOAD_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FileUploadAnswer extends Answer {
    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_MAX_FILE_SIZE)
    private List<@Valid UploadedFile> files;

    @Override
    public void correctAndValidate() {
        if (isDuplicated(files)) {
            throw new MryException(UPLOAD_FILE_ID_DUPLICATED, "上传文件ID不能重复。", "controlId", this.getControlId());
        }
    }

    @Override
    public boolean isFilled() {
        return isNotEmpty(files);
    }

    @Override
    public void clean(Control control) {
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return null;
    }

    @Override
    protected Double doGetIndexedSortableValue() {
        return null;
    }

    @Override
    protected Set<String> doGetSearchableValues() {
        return null;
    }

    @Override
    protected AttributeValue doGetAttributeValue(Attribute attribute, Control control) {
        return new FilesAttributeValue(attribute, files);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new FilesDisplayValue(this.getControlId(), files);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return null;
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    public static FileUploadAnswer.FileUploadAnswerBuilder<?, ?> answerBuilder(FFileUploadControl control) {
        return FileUploadAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
