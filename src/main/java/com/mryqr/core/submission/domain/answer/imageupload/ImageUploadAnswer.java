package com.mryqr.core.submission.domain.answer.imageupload;


import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FImageUploadControl;
import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.FilesDisplayValue;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.collection.NoNullElement;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.ImagesAttributeValue;
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

import static com.mryqr.core.app.domain.page.control.FImageUploadControl.MAX_MAX_IMAGE_SIZE;
import static com.mryqr.core.common.exception.ErrorCode.UPLOAD_IMAGE_ID_DUPLICATED;
import static com.mryqr.core.common.utils.Identified.isDuplicated;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@SuperBuilder
@TypeAlias("IMAGE_UPLOAD_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class ImageUploadAnswer extends Answer {
    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_MAX_IMAGE_SIZE)
    private List<@Valid UploadedFile> images;

    @Override
    public void correctAndValidate() {
        if (isDuplicated(images)) {
            throw new MryException(UPLOAD_IMAGE_ID_DUPLICATED, "上传图片ID不能重复。", "controlId", this.getControlId());
        }
    }

    @Override
    public boolean isFilled() {
        return isNotEmpty(images);
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
        return new ImagesAttributeValue(attribute, images);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new FilesDisplayValue(this.getControlId(), images);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return null;
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    public static ImageUploadAnswer.ImageUploadAnswerBuilder<?, ?> answerBuilder(FImageUploadControl control) {
        return ImageUploadAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
