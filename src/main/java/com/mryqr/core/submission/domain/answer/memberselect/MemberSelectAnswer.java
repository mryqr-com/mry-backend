package com.mryqr.core.submission.domain.answer.memberselect;


import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.TextDisplayValue;
import com.mryqr.common.validation.collection.NoBlankString;
import com.mryqr.common.validation.collection.NoDuplicatedString;
import com.mryqr.common.validation.id.member.MemberId;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FMemberSelectControl;
import com.mryqr.core.member.domain.MemberAware;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.MembersAttributeValue;
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
import java.util.Objects;
import java.util.Set;

import static com.mryqr.core.app.domain.page.control.FMemberSelectControl.MAX_MEMBER_SELECTION;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@SuperBuilder
@TypeAlias("MEMBER_SELECT_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class MemberSelectAnswer extends Answer implements MemberAware {

    @Valid
    @NotNull
    @NoBlankString
    @NoDuplicatedString
    @Size(max = MAX_MEMBER_SELECTION)
    private List<@MemberId String> memberIds;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return isNotEmpty(memberIds);
    }

    @Override
    public void clean(Control control) {
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return Set.copyOf(memberIds);
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
        return new MembersAttributeValue(attribute, memberIds);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        String names = this.memberIds.stream()
                .map(context::memberNameOf)
                .filter(Objects::nonNull)
                .collect(joining(", "));

        return new TextDisplayValue(this.getControlId(), names);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return this.memberIds.stream()
                .map(context::memberNameOf)
                .filter(Objects::nonNull)
                .collect(joining(", "));
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    @Override
    public Set<String> awaredMemberIds() {
        return isNotEmpty(memberIds) ? Set.copyOf(memberIds) : Set.of();
    }

    public static MemberSelectAnswer.MemberSelectAnswerBuilder<?, ?> answerBuilder(FMemberSelectControl control) {
        return MemberSelectAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
