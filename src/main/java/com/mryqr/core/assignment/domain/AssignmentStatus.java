package com.mryqr.core.assignment.domain;

public enum AssignmentStatus {
    IN_PROGRESS(false, "进行中"),
    NEAR_EXPIRE(false, "即将超期"),
    SUCCEED(true, "正常完成"),
    FAILED(true, "超期未完成");

    private final boolean closed;
    private final String name;

    AssignmentStatus(boolean closed, String name) {
        this.closed = closed;
        this.name = name;
    }

    public boolean isClosed() {
        return closed;
    }

    public String getName() {
        return name;
    }
}
