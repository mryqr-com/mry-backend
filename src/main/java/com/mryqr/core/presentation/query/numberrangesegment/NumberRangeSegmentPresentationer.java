package com.mryqr.core.presentation.query.numberrangesegment;

import com.mryqr.common.domain.report.NumberRangeSegment;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PNumberRangeSegmentControl;
import com.mryqr.core.presentation.query.ControlPresentationer;
import com.mryqr.core.presentation.query.QControlPresentation;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.query.report.SubmissionNumberRangeSegmentReporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.core.app.domain.page.control.ControlType.NUMBER_RANGE_SEGMENT;

@Component
@RequiredArgsConstructor
public class NumberRangeSegmentPresentationer implements ControlPresentationer {
    private final SubmissionNumberRangeSegmentReporter submissionNumberRangeSegmentReporter;

    @Override
    public boolean canHandle(Control control) {
        return control.getType() == NUMBER_RANGE_SEGMENT;
    }

    @Override
    public QControlPresentation present(QR qr, Control control, App app) {
        PNumberRangeSegmentControl theControl = (PNumberRangeSegmentControl) control;

        List<NumberRangeSegment> segments = submissionNumberRangeSegmentReporter.reportForQr(qr.getId(),
                theControl.getSegmentType(),
                theControl.getPageId(),
                theControl.getBasedControlId(),
                theControl.getTargetControlId(),
                theControl.getRange(),
                theControl.getNumberRanges(),
                app
        );

        return new QNumberRangeSegmentPresentation(theControl.getNumberRanges(), segments);
    }
}
