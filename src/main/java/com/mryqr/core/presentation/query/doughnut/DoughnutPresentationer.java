package com.mryqr.core.presentation.query.doughnut;

import com.mryqr.common.domain.report.CategorizedOptionSegment;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PDoughnutControl;
import com.mryqr.core.presentation.query.ControlPresentationer;
import com.mryqr.core.presentation.query.QControlPresentation;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.query.report.SubmissionCategorizedAnswerSegmentReporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.core.app.domain.page.control.ControlType.DOUGHNUT;

@Component
@RequiredArgsConstructor
public class DoughnutPresentationer implements ControlPresentationer {
    private final SubmissionCategorizedAnswerSegmentReporter submissionCategorizedAnswerSegmentReporter;

    @Override
    public boolean canHandle(Control control) {
        return control.getType() == DOUGHNUT;
    }

    @Override
    public QControlPresentation present(QR qr, Control control, App app) {
        PDoughnutControl theControl = (PDoughnutControl) control;

        List<CategorizedOptionSegment> segments = submissionCategorizedAnswerSegmentReporter.reportForQr(qr.getId(),
                theControl.getSegmentType(),
                theControl.getPageId(),
                theControl.getBasedControlId(),
                theControl.getTargetControlId(),
                theControl.getRange(),
                app);

        return new QDoughnutPresentation(segments);
    }
}
