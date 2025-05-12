package com.mryqr.common.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class EasyExcelResult {
    private final List<List<String>> headers;
    private final List<List<Object>> records;
    private final String fileName;
}
