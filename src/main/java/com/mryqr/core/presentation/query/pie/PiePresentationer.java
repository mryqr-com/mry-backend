package com.mryqr.core.presentation.query.pie;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PPieControl;
import com.mryqr.core.common.domain.report.CategorizedOptionSegment;
import com.mryqr.core.presentation.query.ControlPresentationer;
import com.mryqr.core.presentation.query.QControlPresentation;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.query.report.SubmissionCategorizedAnswerSegmentReporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.core.app.domain.page.control.ControlType.PIE;

@Component
@RequiredArgsConstructor
public class PiePresentationer implements ControlPresentationer {
    private final SubmissionCategorizedAnswerSegmentReporter submissionCategorizedAnswerSegmentReporter;

    @Override
    public boolean canHandle(Control control) {
        return control.getType() == PIE;
    }

    @Override
    public QControlPresentation present(QR qr, Control control, App app) {
        PPieControl theControl = (PPieControl) control;

        List<CategorizedOptionSegment> segments = submissionCategorizedAnswerSegmentReporter.reportForQr(qr.getId(),
                theControl.getSegmentType(),
                theControl.getPageId(),
                theControl.getBasedControlId(),
                theControl.getTargetControlId(),
                theControl.getRange(),
                app);

        return new QPiePresentation(segments);
    }
}
