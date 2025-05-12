package com.mryqr.core.member.command.importmember;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberImporter {
    private final MemberImportParser memberImportParser;
    private final MemberImportSaver memberImportSaver;

    public MemberImportResponse importMembers(InputStream inputStream, int limit, User user) {
        List<MemberImportRecord> records = memberImportParser.parse(inputStream, user.getTenantId(), limit);

        records.forEach(record -> {
            try {
                if (!record.hasError()) {
                    memberImportSaver.save(record, user);
                }
            } catch (MryException mryException) {
                record.addError(mryException.getUserMessage());
            }
        });

        List<MemberImportRecord> errorRecords = records.stream().filter(MemberImportRecord::hasError).collect(toImmutableList());

        return MemberImportResponse.builder()
                .readCount(records.size())
                .importedCount(records.size() - errorRecords.size())
                .errorRecords(errorRecords)
                .build();
    }
}
