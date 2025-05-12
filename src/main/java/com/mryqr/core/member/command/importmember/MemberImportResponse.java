package com.mryqr.core.member.command.importmember;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MemberImportResponse {
    private final int readCount;
    private final int importedCount;
    private final List<MemberImportRecord> errorRecords;
}
