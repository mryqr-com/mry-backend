package com.mryqr.common.domain.display;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class EmailedMemberDisplayValue extends DisplayValue {
    private EmailedMember member;

    public EmailedMemberDisplayValue(String key, EmailedMember member) {
        super(key, DisplayValueType.EMAILED_MEMBER_DISPLAY_VALUE);
        this.member = member;
    }
}
