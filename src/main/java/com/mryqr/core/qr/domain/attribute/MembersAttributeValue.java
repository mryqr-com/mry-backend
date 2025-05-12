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

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@TypeAlias("MEMBERS_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class MembersAttributeValue extends AttributeValue implements MemberAware {
    private List<String> memberIds;

    public MembersAttributeValue(Attribute attribute, List<String> memberIds) {
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
        String names = this.memberIds.stream()
                .map(context::memberNameOf)
                .filter(Objects::nonNull)
                .collect(joining(", "));

        if (isBlank(names)) {
            return null;
        }

        return new TextDisplayValue(this.getAttributeId(), names);
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
            return memberReference == null ? null : memberReference.getName();
        }).filter(Objects::nonNull).collect(joining(", "));
    }

    @Override
    public Set<String> awaredMemberIds() {
        return isNotEmpty(memberIds) ? Set.copyOf(memberIds) : Set.of();
    }
}
