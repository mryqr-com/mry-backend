package com.mryqr.core.common.utils;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PagedList<T> {
    private final int totalNumber;
    private final int pageIndex;
    private final int pageSize;
    private final List<T> data;
}
