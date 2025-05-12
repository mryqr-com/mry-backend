package com.mryqr.core.submission.domain;

import com.mryqr.core.member.domain.MemberReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class SubmissionReferenceContext {
    private final Map<String, MemberReference> memberReferences;

    public String memberNameOf(String memberId) {
        if (memberReferences == null) {
            return null;
        }

        MemberReference reference = memberReferences.get(memberId);
        return reference != null ? reference.getName() : null;
    }

}
