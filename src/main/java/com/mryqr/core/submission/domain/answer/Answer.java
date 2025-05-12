package com.mryqr.core.submission.domain.answer;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.indexedfield.IndexedValue;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.id.control.ControlId;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.address.AddressAnswer;
import com.mryqr.core.submission.domain.answer.checkbox.CheckboxAnswer;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.email.EmailAnswer;
import com.mryqr.core.submission.domain.answer.fileupload.FileUploadAnswer;
import com.mryqr.core.submission.domain.answer.geolocation.GeolocationAnswer;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import com.mryqr.core.submission.domain.answer.imageupload.ImageUploadAnswer;
import com.mryqr.core.submission.domain.answer.itemcount.ItemCountAnswer;
import com.mryqr.core.submission.domain.answer.itemstatus.ItemStatusAnswer;
import com.mryqr.core.submission.domain.answer.memberselect.MemberSelectAnswer;
import com.mryqr.core.submission.domain.answer.mobilenumber.MobileNumberAnswer;
import com.mryqr.core.submission.domain.answer.multilevelselection.MultiLevelSelectionAnswer;
import com.mryqr.core.submission.domain.answer.multilinetext.MultiLineTextAnswer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.core.submission.domain.answer.numberranking.NumberRankingAnswer;
import com.mryqr.core.submission.domain.answer.personname.PersonNameAnswer;
import com.mryqr.core.submission.domain.answer.pointcheck.PointCheckAnswer;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.core.submission.domain.answer.richtext.RichTextInputAnswer;
import com.mryqr.core.submission.domain.answer.signature.SignatureAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.submission.domain.answer.time.TimeAnswer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

import static com.mryqr.common.exception.ErrorCode.ANSWERS_DUPLICATED;
import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "controlType",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = RadioAnswer.class, name = "RADIO"),
        @JsonSubTypes.Type(value = CheckboxAnswer.class, name = "CHECKBOX"),
        @JsonSubTypes.Type(value = MemberSelectAnswer.class, name = "MEMBER_SELECT"),
        @JsonSubTypes.Type(value = SingleLineTextAnswer.class, name = "SINGLE_LINE_TEXT"),
        @JsonSubTypes.Type(value = IdentifierAnswer.class, name = "IDENTIFIER"),
        @JsonSubTypes.Type(value = PersonNameAnswer.class, name = "PERSON_NAME"),
        @JsonSubTypes.Type(value = MultiLineTextAnswer.class, name = "MULTI_LINE_TEXT"),
        @JsonSubTypes.Type(value = RichTextInputAnswer.class, name = "RICH_TEXT_INPUT"),
        @JsonSubTypes.Type(value = DropdownAnswer.class, name = "DROPDOWN"),
        @JsonSubTypes.Type(value = FileUploadAnswer.class, name = "FILE_UPLOAD"),
        @JsonSubTypes.Type(value = ImageUploadAnswer.class, name = "IMAGE_UPLOAD"),
        @JsonSubTypes.Type(value = NumberInputAnswer.class, name = "NUMBER_INPUT"),
        @JsonSubTypes.Type(value = NumberRankingAnswer.class, name = "NUMBER_RANKING"),
        @JsonSubTypes.Type(value = MobileNumberAnswer.class, name = "MOBILE"),
        @JsonSubTypes.Type(value = EmailAnswer.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = DateAnswer.class, name = "DATE"),
        @JsonSubTypes.Type(value = TimeAnswer.class, name = "TIME"),
        @JsonSubTypes.Type(value = ItemStatusAnswer.class, name = "ITEM_STATUS"),
        @JsonSubTypes.Type(value = ItemCountAnswer.class, name = "ITEM_COUNT"),
        @JsonSubTypes.Type(value = PointCheckAnswer.class, name = "POINT_CHECK"),
        @JsonSubTypes.Type(value = GeolocationAnswer.class, name = "GEOLOCATION"),
        @JsonSubTypes.Type(value = AddressAnswer.class, name = "ADDRESS"),
        @JsonSubTypes.Type(value = SignatureAnswer.class, name = "SIGNATURE"),
        @JsonSubTypes.Type(value = MultiLevelSelectionAnswer.class, name = "MULTI_LEVEL_SELECTION"),
})

@Getter
@SuperBuilder
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED)
public abstract class Answer {
    @NotBlank
    @ControlId
    private String controlId;

    @NotNull
    private ControlType controlType;

    public static void checkNoDuplicatedAnswers(Set<Answer> answers) {
        long count = answers.stream().map(Answer::getControlId).distinct().count();
        if (count != answers.size()) {
            throw new MryException(ANSWERS_DUPLICATED, "答案重复。");
        }
    }

    public final IndexedValue indexedValue() {
        if (!controlType.isAnswerIndexable()) {
            return null;
        }

        Set<String> indexedTextValues = indexedTextValues();
        Double indexedSortableValue = indexedSortableValue();
        if (indexedSortableValue == null && isEmpty(indexedTextValues)) {
            return null;
        }

        return IndexedValue.builder()
                .rid(controlId)
                .sv(indexedSortableValue)
                .tv(isNotEmpty(indexedTextValues) ? indexedTextValues : null)
                .build();
    }

    private Set<String> indexedTextValues() {
        return (isFilled() && controlType.isAnswerTextable()) ? doGetIndexedTextValues() : null;
    }

    private Double indexedSortableValue() {
        return (isFilled() && controlType.isAnswerSortable()) ? doGetIndexedSortableValue() : null;
    }

    public final Set<String> searchableValues() {//获取可搜索值，可能返回null
        return (isFilled() && controlType.isAnswerSearchable()) ? doGetSearchableValues() : null;
    }

    public final AttributeValue toAttributeValue(Attribute attribute, Control control) {//获取属性值，可能返回null
        return isFilled() ? doGetAttributeValue(attribute, control) : null;
    }

    public final DisplayValue toDisplayValue(SubmissionReferenceContext context) {//获取展示值，用于展示控件
        return isFilled() ? doGetDisplayValue(context) : null;
    }

    public final String toExportValue(Control control, SubmissionReferenceContext context) {
        return (isFilled() && controlType.isAnswerExportable()) ? doGetExportValue(control, context) : null;
    }

    public final Double calculateNumericalValue(Control control) {
        return (isFilled() && control.isAnswerNumerical()) ? doCalculateNumericalValue(control) : null;
    }

    public abstract void correctAndValidate();//处理提交时，首先进行answer的自我更正，无需借助其他对象

    public abstract boolean isFilled();//判断answer是否有值，没有提供值的answer将不会落库

    public abstract void clean(Control control);//housekeep落库时清洗一次，比如剔除掉不存在的optionId等

    protected abstract Set<String> doGetIndexedTextValues();//获取索引文本值，如果没有值则返回null

    protected abstract Double doGetIndexedSortableValue();//获取可索引排序值，如果没有值则返回null

    protected abstract Set<String> doGetSearchableValues();//获取可搜索值，如果没有值则返回null

    protected abstract AttributeValue doGetAttributeValue(Attribute attribute, Control control);//获取属性值

    protected abstract DisplayValue doGetDisplayValue(SubmissionReferenceContext context);//获取展示值

    protected abstract String doGetExportValue(Control control, SubmissionReferenceContext context);

    protected abstract Double doCalculateNumericalValue(Control control);//获取answer对应的数值
}
