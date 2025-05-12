package com.mryqr.core.qr.domain.attribute;

import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.EmailedMember;
import com.mryqr.common.domain.display.EmailedMembersDisplayValue;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.member.domain.MemberAware;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@TypeAlias("MEMBERS_EMAIL_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class MembersEmailAttributeValue extends AttributeValue implements MemberAware {
    private List<String> memberIds;

    public MembersEmailAttributeValue(Attribute attribute, List<String> memberIds) {
        super(attribute);
        this.memberIds = memberIds;
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
    protected DisplayValue doGetDisplayValue(QrReferenceContext context) {
        List<EmailedMember> emailedMembers = this.memberIds.stream().map(memberId -> {
            MemberReference member = context.memberOf(memberId);
            if (member == null) {
                return null;
            }
            return EmailedMember.builder().id(member.getId()).name(member.getName()).email(member.getEmail()).build();
        }).filter(Objects::nonNull).collect(toImmutableList());

        if (isEmpty(emailedMembers)) {
            return null;
        }

        return new EmailedMembersDisplayValue(this.getAttributeId(), emailedMembers);
    }

    @Override
    public boolean isFilled() {
        return isNotEmpty(memberIds);
    }

    @Override
    public void clean(App app) {
    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        return memberIds.stream().map(id -> {
            MemberReference memberReference = context.memberOf(id);
            return memberReference == null ? null : memberReference.memberWithEmailText();
        }).filter(Objects::nonNull).collect(joining(", "));
    }

    @Override
    public Set<String> awaredMemberIds() {
        return isNotEmpty(memberIds) ? Set.copyOf(memberIds) : Set.of();
    }
}
