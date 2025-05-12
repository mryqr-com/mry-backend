package com.mryqr.core.qr.domain.attribute;

import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.TextDisplayValue;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.group.domain.GroupAware;
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
@TypeAlias("GROUP_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class GroupAttributeValue extends AttributeValue implements GroupAware {
    private String groupId;

    public GroupAttributeValue(Attribute attribute, String groupId) {
        super(attribute);
        this.groupId = groupId;
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return Set.of(groupId);
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
        String groupName = context.groupNameOf(this.groupId);
        if (isBlank(groupName)) {
            return null;
        }

        return new TextDisplayValue(this.getAttributeId(), groupName);
    }

    @Override
    public boolean isFilled() {
        return isNotBlank(groupId);
    }

    @Override
    public void clean(App app) {
    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        return context.groupNameOf(groupId);
    }

    @Override
    public Set<String> awaredGroupIds() {
        return isNotBlank(this.groupId) ? Set.of(this.groupId) : Set.of();
    }
}
