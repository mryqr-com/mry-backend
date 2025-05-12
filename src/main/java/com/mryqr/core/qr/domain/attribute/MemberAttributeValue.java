package com.mryqr.core.qr.domain.attribute;

import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.TextDisplayValue;
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

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@TypeAlias("MEMBER_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class MemberAttributeValue extends AttributeValue implements MemberAware {
    private String memberId;

    public MemberAttributeValue(Attribute attribute, String memberId) {
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
        String memberName = context.memberNameOf(this.memberId);
        if (isBlank(memberName)) {
            return null;
        }

        return new TextDisplayValue(this.getAttributeId(), memberName);
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
        return memberReference == null ? null : memberReference.getName();
    }

    @Override
    public Set<String> awaredMemberIds() {
        return isNotBlank(memberId) ? Set.of(memberId) : Set.of();
    }
}
