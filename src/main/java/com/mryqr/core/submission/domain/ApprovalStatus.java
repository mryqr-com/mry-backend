package com.mryqr.core.submission.domain;

public enum ApprovalStatus {
    PASSED,
    NOT_PASSED,
    NONE;

    public static ApprovalStatus statusOf(SubmissionApproval approval) {
        if (approval == null) {
            return NONE;
        }
        return approval.isPassed() ? PASSED : NOT_PASSED;
    }
}
