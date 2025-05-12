package com.mryqr.core.qr.domain.attribute;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeType;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.common.domain.ValueType;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "valueType",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = TextAttributeValue.class, name = "TEXT_VALUE"),
        @JsonSubTypes.Type(value = MultiLineTextAttributeValue.class, name = "MULTI_LINE_TEXT_VALUE"),
        @JsonSubTypes.Type(value = TimestampAttributeValue.class, name = "TIMESTAMP_VALUE"),
        @JsonSubTypes.Type(value = LocalDateAttributeValue.class, name = "LOCAL_DATE_VALUE"),
        @JsonSubTypes.Type(value = LocalTimeAttributeValue.class, name = "LOCAL_TIME_VALUE"),
        @JsonSubTypes.Type(value = DoubleAttributeValue.class, name = "DOUBLE_VALUE"),
        @JsonSubTypes.Type(value = IntegerAttributeValue.class, name = "INTEGER_VALUE"),
        @JsonSubTypes.Type(value = PointCheckAttributeValue.class, name = "POINT_CHECK_VALUE"),
        @JsonSubTypes.Type(value = MobileAttributeValue.class, name = "MOBILE_VALUE"),
        @JsonSubTypes.Type(value = EmailAttributeValue.class, name = "EMAIL_VALUE"),
        @JsonSubTypes.Type(value = IdentifierAttributeValue.class, name = "IDENTIFIER_VALUE"),
        @JsonSubTypes.Type(value = AddressAttributeValue.class, name = "ADDRESS_VALUE"),
        @JsonSubTypes.Type(value = GeolocationAttributeValue.class, name = "GEOLOCATION_VALUE"),
        @JsonSubTypes.Type(value = GroupAttributeValue.class, name = "GROUP_VALUE"),
        @JsonSubTypes.Type(value = MemberAttributeValue.class, name = "MEMBER_VALUE"),
        @JsonSubTypes.Type(value = MemberMobileAttributeValue.class, name = "MEMBER_MOBILE_VALUE"),
        @JsonSubTypes.Type(value = MemberEmailAttributeValue.class, name = "MEMBER_EMAIL_VALUE"),
        @JsonSubTypes.Type(value = MembersAttributeValue.class, name = "MEMBERS_VALUE"),
        @JsonSubTypes.Type(value = MembersMobileAttributeValue.class, name = "MEMBERS_MOBILE_VALUE"),
        @JsonSubTypes.Type(value = MembersEmailAttributeValue.class, name = "MEMBERS_EMAIL_VALUE"),
        @JsonSubTypes.Type(value = RadioAttributeValue.class, name = "RADIO_VALUE"),
        @JsonSubTypes.Type(value = CheckboxAttributeValue.class, name = "CHECKBOX_VALUE"),
        @JsonSubTypes.Type(value = DropdownAttributeValue.class, name = "DROPDOWN_VALUE"),
        @JsonSubTypes.Type(value = ItemStatusAttributeValue.class, name = "ITEM_STATUS_VALUE"),
        @JsonSubTypes.Type(value = ItemCountAttributeValue.class, name = "ITEM_COUNT_VALUE"),
        @JsonSubTypes.Type(value = FilesAttributeValue.class, name = "FILES_VALUE"),
        @JsonSubTypes.Type(value = ImagesAttributeValue.class, name = "IMAGES_VALUE"),
        @JsonSubTypes.Type(value = SignatureAttributeValue.class, name = "SIGNATURE_VALUE"),
        @JsonSubTypes.Type(value = MultiLevelSelectionAttributeValue.class, name = "MULTI_LEVEL_SELECTION_VALUE"),
        @JsonSubTypes.Type(value = BooleanAttributeValue.class, name = "BOOLEAN_VALUE"),
        @JsonSubTypes.Type(value = CirculationStatusAttributeValue.class, name = "CIRCULATION_STATUS_VALUE"),
})

@Getter
@Document
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED)
public abstract class AttributeValue {
    private String attributeId;
    private AttributeType attributeType;
    private ValueType valueType;

    public AttributeValue(Attribute attribute) {
        this.attributeId = attribute.getId();
        this.attributeType = attribute.getType();
        this.valueType = attribute.getValueType();
    }

    public final IndexedValue indexedValue() {
        if (!valueType.isIndexable()) {
            return null;
        }

        Set<String> indexedTextValues = indexedTextValues();
        Double indexedSortableValue = indexedSortableValue();
        if (indexedSortableValue == null && isEmpty(indexedTextValues)) {
            return null;
        }

        return IndexedValue.builder()
                .rid(attributeId)
                .sv(indexedSortableValue)
                .tv(isNotEmpty(indexedTextValues) ? indexedTextValues : null)
                .build();
    }

    public Set<String> indexedTextValues() {
        return (isFilled() && valueType.isTextable()) ? doGetIndexedTextValues() : null;
    }

    public Double indexedSortableValue() {
        return (isFilled() && valueType.isSortable()) ? doGetIndexedSortableValue() : null;
    }

    public final Set<String> searchableValues() {//获取可搜索值，可能返回null
        return (isFilled() && valueType.isSearchable()) ? doGetSearchableValues() : null;
    }

    public final DisplayValue toDisplayValue(QrReferenceContext context) {//获取展示值，用于展示控件
        return isFilled() ? doGetDisplayValue(context) : null;
    }

    public final String toExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        return (isFilled() && valueType.isExportable()) ? doGetExportValue(attribute, context, refControl) : null;
    }

    protected abstract Set<String> doGetIndexedTextValues();//获取索引文本值，如果没有值则返回null

    protected abstract Double doGetIndexedSortableValue();//获取可索引排序值，如果没有值则返回null

    protected abstract Set<String> doGetSearchableValues();//获取可搜索值，如果没有值则返回null

    protected abstract DisplayValue doGetDisplayValue(QrReferenceContext context);//获取展示值

    protected abstract String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl);

    public abstract boolean isFilled();//判断是否有值，没有提供值的将不会落库

    public abstract void clean(App app);//清洗，比如清除掉不存在的optionId等

}
