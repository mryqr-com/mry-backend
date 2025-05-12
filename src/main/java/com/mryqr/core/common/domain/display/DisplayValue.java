package com.mryqr.core.common.domain.display;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = TextDisplayValue.class, name = "TEXT_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = AddressDisplayValue.class, name = "ADDRESS_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = TextOptionsDisplayValue.class, name = "TEXT_OPTIONS_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = FilesDisplayValue.class, name = "FILES_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = GeolocationDisplayValue.class, name = "GEOLOCATION_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = ItemCountDisplayValue.class, name = "ITEM_COUNT_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = TextOptionDisplayValue.class, name = "TEXT_OPTION_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = PointCheckDisplayValue.class, name = "POINT_CHECK_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = BooleanDisplayValue.class, name = "BOOLEAN_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = NumberDisplayValue.class, name = "NUMBER_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = EmailedMemberDisplayValue.class, name = "EMAILED_MEMBER_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = MobiledMemberDisplayValue.class, name = "MOBILE_MEMBER_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = EmailedMembersDisplayValue.class, name = "EMAILED_MEMBERS_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = MobiledMembersDisplayValue.class, name = "MOBILE_MEMBERS_DISPLAY_VALUE"),
        @JsonSubTypes.Type(value = TimestampDisplayValue.class, name = "TIMESTAMP_DISPLAY_VALUE"),
})

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED)
public abstract class DisplayValue {
    private String key;
    private DisplayValueType type;

    protected DisplayValue(String key, DisplayValueType type) {
        this.key = key;
        this.type = type;
    }
}
