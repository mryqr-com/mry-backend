package com.mryqr.core.presentation.query.bar;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PBarControl;
import com.mryqr.core.presentation.query.ControlPresentationer;
import com.mryqr.core.presentation.query.QControlPresentation;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.query.report.SubmissionCategorizedAnswerSegmentReporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.control.ControlType.BAR;

@Component
@RequiredArgsConstructor
public class BarPresentationer implements ControlPresentationer {
    private final SubmissionCategorizedAnswerSegmentReporter submissionCategorizedAnswerSegmentReporter;

    @Override
    public boolean canHandle(Control control) {
        return control.getType() == BAR;
    }

    @Override
    public QControlPresentation present(QR qr, Control control, App app) {
        PBarControl theControl = (PBarControl) control;

        return new QBarPresentation(submissionCategorizedAnswerSegmentReporter.reportForQrMultiple(qr.getId(),
                theControl.getSegmentType(),
                theControl.getPageId(),
                theControl.getBasedControlId(),
                theControl.getTargetControlIds(),
                theControl.getRange(),
                app));
    }
}
