package com.mryqr.core.submission.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncSubmissionGroupFromQrTask implements RepeatableTask {
    private final QrRepository qrRepository;
    private final SubmissionRepository submissionRepository;

    public void run(String qrId) {
        qrRepository.byIdOptional(qrId).ifPresent(qr -> {
            int count = submissionRepository.syncGroupFromQr(qr);
            log.info("Synced group ID for all {} submissions with qr[{}].", count, qrId);
        });
    }
}
