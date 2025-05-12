package com.mryqr.core.qr.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.member.domain.MemberReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QrReferenceContext {
    private final App app;
    private final Map<String, MemberReference> memberReferences;
    private final Map<String, String> groupFullNames;

    public String memberNameOf(String memberId) {
        if (memberReferences == null) {
            return null;
        }

        MemberReference reference = memberReferences.get(memberId);
        return reference != null ? reference.getName() : null;
    }

    public MemberReference memberOf(String memberId) {
        if (memberReferences == null) {
            return null;
        }

        return memberReferences.get(memberId);
    }

    public String groupNameOf(String groupId) {
        return groupFullNames.get(groupId);
    }
}
