package com.mryqr.core.submission.domain;

import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.qr.domain.QR;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreateSubmissionWithQrResult {
    private final Submission submission;
    private final QR qr;
    private final Plate plate;

}
