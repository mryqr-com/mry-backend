package com.mryqr.core.qr.command.importqr;

import com.mryqr.common.domain.user.User;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.qr.domain.PlatedQr;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrFactory;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionFactory;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrImportBatchSaver {
    private final QrRepository qrRepository;
    private final PlateRepository plateRepository;
    private final SubmissionRepository submissionRepository;
    private final QrFactory qrFactory;
    private final SubmissionFactory submissionFactory;

    @Transactional(propagation = REQUIRES_NEW)
    public void save(List<QrImportRecord> records, Group group, App app, User user) {
        List<QR> qrs = new ArrayList<>();
        List<Submission> submissions = new ArrayList<>();
        List<Plate> plates = new ArrayList<>();

        records.forEach(record -> {
            PlatedQr platedQr = qrFactory.createImportedPlatedQr(record.getName(), group, app, record.getCustomId(), user);
            QR qr = platedQr.getQr();
            qr.putAttributeValues(record.getAttributeValues(), user);
            qrs.add(qr);

            Plate plate = platedQr.getPlate();
            plates.add(plate);

            Map<String, Set<Answer>> answers = record.getAnswers();
            if (isNotEmpty(answers)) {
                List<Submission> recordSubmissions = answers.entrySet().stream()
                        .map(entry -> submissionFactory.createImportQrSubmission(entry.getValue(), entry.getKey(), qr, app))
                        .collect(toImmutableList());
                submissions.addAll(recordSubmissions);
            }
        });

        qrRepository.insert(qrs);
        plateRepository.insert(plates);
        submissionRepository.insert(submissions);
    }

}
