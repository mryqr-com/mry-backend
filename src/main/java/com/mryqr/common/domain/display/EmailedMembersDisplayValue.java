package com.mryqr.common.domain.display;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class EmailedMembersDisplayValue extends DisplayValue {
    private List<EmailedMember> members;

    public EmailedMembersDisplayValue(String key, List<EmailedMember> members) {
        super(key, DisplayValueType.EMAILED_MEMBERS_DISPLAY_VALUE);
        this.members = members;
    }
}
