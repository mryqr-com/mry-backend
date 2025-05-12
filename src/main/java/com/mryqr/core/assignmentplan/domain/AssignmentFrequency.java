package com.mryqr.core.assignmentplan.domain;

public enum AssignmentFrequency {
    EVERY_DAY(true),
    EVERY_WEEK(true),
    EVERY_MONTH(false),
    EVERY_THREE_MONTH(false),
    EVERY_SIX_MONTH(false),
    EVERY_YEAR(false);

    private final boolean fixedCycle;

    AssignmentFrequency(boolean fixedTimeCycle) {
        this.fixedCycle = fixedTimeCycle;
    }

    public boolean isFixedTimeCycle() {
        return fixedCycle;
    }
}
