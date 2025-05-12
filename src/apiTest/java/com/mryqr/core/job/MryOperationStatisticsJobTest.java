package com.mryqr.core.job;

import com.mryqr.BaseApiTest;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.management.operation.MrySelfOperationJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static com.mryqr.management.operation.MryOperationApp.*;
import static java.time.ZoneId.systemDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MryOperationStatisticsJobTest extends BaseApiTest {
    @Autowired
    private MrySelfOperationJob mrySelfOperationJob;

    @Test
    public void should_run() {
        mrySelfOperationJob.run();
        QR qr = qrRepository.byCustomId(MRY_OPERATION_APP_ID, OPERATION_QR_CUSTOM_ID);
        assertNotNull(qr);

        Submission totalSubmission = submissionRepository.lastInstanceSubmission(qr.getId(), SYNC_TOTAL_PAGE_ID).get();
        assertEquals(LocalDate.now(), LocalDate.ofInstant(totalSubmission.getCreatedAt(), systemDefault()));

        Submission deltaSubmission = submissionRepository.lastInstanceSubmission(qr.getId(), SYNC_DELTA_PAGE_ID).get();
        assertEquals(LocalDate.now(), LocalDate.ofInstant(deltaSubmission.getCreatedAt(), systemDefault()));
    }
}
