package com.mryqr.core.common.domain.display;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class MobiledMemberDisplayValue extends DisplayValue {
    private MobiledMember member;

    public MobiledMemberDisplayValue(String key, MobiledMember member) {
        super(key, DisplayValueType.MOBILE_MEMBER_DISPLAY_VALUE);
        this.member = member;
    }
}
