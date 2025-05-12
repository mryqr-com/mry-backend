package com.mryqr.core.member.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MemberReference {
    private final String id;
    private final String name;
    private final String mobile;
    private final String email;

    public String memberWithEmailText() {
        if (isBlank(email)) {
            return name;
        }
        return name + "(" + email + ")";
    }

    public String memberWithMobileText() {
        if (isBlank(mobile)) {
            return name;
        }
        return name + "(" + mobile + ")";
    }
}
