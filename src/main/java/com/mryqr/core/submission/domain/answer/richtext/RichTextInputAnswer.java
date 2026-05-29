package com.mryqr.core.submission.domain.answer.richtext;


import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.TextDisplayValue;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FRichTextInputControl;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.RichTextAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.common.utils.MryConstants.MAX_PARAGRAPH_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("RICH_TEXT_INPUT_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class RichTextInputAnswer extends Answer {

    @Size(max = MAX_PARAGRAPH_LENGTH)
    private String content;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return isNotBlank(content);
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
        return new RichTextAttributeValue(attribute, content);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new TextDisplayValue(this.getControlId(), content);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return null;
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    public static RichTextInputAnswer.RichTextInputAnswerBuilder<?, ?> answerBuilder(FRichTextInputControl control) {
        return RichTextInputAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
