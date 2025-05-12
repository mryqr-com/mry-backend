package com.mryqr.core.common.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.mryqr.core.common.exception.MryException.requestValidationException;

@Getter
@EqualsAndHashCode
public class Pagination {
    private final int pageIndex;
    private final int pageSize;

    private Pagination(int pageIndex, int pageSize) {
        if (pageIndex < 1) {
            throw requestValidationException("detail", "pageIndex不能小于1");
        }

        if (pageIndex > 10000) {
            throw requestValidationException("detail", "pageIndex不能大于10000");
        }

        if (pageSize < 10) {
            throw requestValidationException("detail", "pageSize不能小于10");
        }

        if (pageSize > 500) {
            throw requestValidationException("detail", "pageSize不能大于500");
        }

        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public static Pagination pagination(int pageIndex, int pageSize) {
        return new Pagination(pageIndex, pageSize);
    }

    public int skip() {
        return (this.pageIndex - 1) * this.pageSize;
    }

    public int limit() {
        return this.pageSize;
    }

}
