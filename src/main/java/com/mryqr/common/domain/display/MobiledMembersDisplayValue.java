package com.mryqr.common.domain.display;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class MobiledMembersDisplayValue extends DisplayValue {
    private List<MobiledMember> members;

    public MobiledMembersDisplayValue(String key, List<MobiledMember> members) {
        super(key, DisplayValueType.MOBILE_MEMBERS_DISPLAY_VALUE);
        this.members = members;
    }
}
