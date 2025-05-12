package com.mryqr.core.qr.command.importqr;

import com.google.common.collect.Lists;
import com.mryqr.common.domain.user.User;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.qr.command.importqr.QrImportResponse.QrImportErrorRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrImporter {
    private final QrImportBatchSaver qrImportBatchSaver;
    private final QrImportParser qrImportParser;

    public QrImportResponse importQrs(InputStream inputStream, Group group, App app, int limit, User user) {
        List<QrImportRecord> records = qrImportParser.parse(inputStream, group.getId(), app, limit);

        List<QrImportRecord> tobeSavedRecords = records.stream()
                .filter(record -> isEmpty(record.getErrors()))
                .collect(toImmutableList());

        AtomicInteger savedCount = new AtomicInteger(0);
        if (isNotEmpty(tobeSavedRecords)) {
            List<List<QrImportRecord>> partitionedBatches = Lists.partition(tobeSavedRecords, 100);
            partitionedBatches.forEach(batchedRecords -> {
                try {
                    qrImportBatchSaver.save(batchedRecords, group, app, user);
                    savedCount.addAndGet(batchedRecords.size());
                } catch (Throwable t) {
                    List<String> failedCustomIds = batchedRecords.stream().map(QrImportRecord::getCustomId).collect(toImmutableList());
                    log.warn("Failed to save imported qr records with customIds:{}.", failedCustomIds, t);
                }
            });
        }

        List<QrImportRecord> errorRecords = records.stream()
                .filter(record -> isNotEmpty(record.getErrors()))
                .collect(toImmutableList());
        List<QrImportErrorRecord> errors = errorRecords.stream()
                .map(record -> QrImportErrorRecord.builder()
                        .rowIndex(record.getRowIndex())
                        .name(record.getName())
                        .customId(record.getCustomId())
                        .errors(record.getErrors())
                        .build())
                .collect(toImmutableList());

        return QrImportResponse.builder()
                .readCount(records.size())
                .importedCount(savedCount.get())
                .errorRecords(errors)
                .build();
    }
}
