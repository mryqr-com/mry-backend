package com.mryqr.core.qr.command.importqr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QrImportResponse {
    private final int readCount;
    private final int importedCount;
    private final List<QrImportErrorRecord> errorRecords;

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class QrImportErrorRecord {
        private final int rowIndex;
        private final String name;
        private final String customId;
        private final List<String> errors;
    }
}
