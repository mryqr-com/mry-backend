package com.mryqr.core.qr.domain.attribute;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.MobiledMember;
import com.mryqr.core.common.domain.display.MobiledMemberDisplayValue;
import com.mryqr.core.member.domain.MemberAware;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@TypeAlias("MEMBER_MOBILE_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class MemberMobileAttributeValue extends AttributeValue implements MemberAware {
    private String memberId;

    public MemberMobileAttributeValue(Attribute attribute, String memberId) {
        super(attribute);
        this.memberId = memberId;
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return Set.of(memberId);
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
        MemberReference member = context.memberOf(this.memberId);
        if (member == null) {
            return null;
        }

        MobiledMember mobiledMember = MobiledMember.builder().name(member.getName()).mobile(member.getMobile()).build();
        return new MobiledMemberDisplayValue(this.getAttributeId(), mobiledMember);
    }

    @Override
    public boolean isFilled() {
        return isNotBlank(memberId);
    }

    @Override
    public void clean(App app) {
    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        MemberReference memberReference = context.memberOf(memberId);
        return memberReference == null ? null : memberReference.memberWithMobileText();
    }

    @Override
    public Set<String> awaredMemberIds() {
        return isNotBlank(memberId) ? Set.of(memberId) : Set.of();
    }
}
