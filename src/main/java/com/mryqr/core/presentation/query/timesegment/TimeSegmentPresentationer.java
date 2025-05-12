package com.mryqr.core.presentation.query.timesegment;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PTimeSegmentControl;
import com.mryqr.core.app.domain.page.control.PTimeSegmentControl.TimeSegmentSetting;
import com.mryqr.core.common.domain.report.TimeSegment;
import com.mryqr.core.presentation.query.ControlPresentationer;
import com.mryqr.core.presentation.query.QControlPresentation;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.query.report.SubmissionTimeSegmentReporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.core.app.domain.page.control.ControlType.TIME_SEGMENT;

@Component
@RequiredArgsConstructor
public class TimeSegmentPresentationer implements ControlPresentationer {
    private final SubmissionTimeSegmentReporter submissionTimeSegmentReporter;

    @Override
    public boolean canHandle(Control control) {
        return control.getType() == TIME_SEGMENT;
    }

    @Override
    public QControlPresentation present(QR qr, Control control, App app) {
        PTimeSegmentControl theControl = (PTimeSegmentControl) control;

        List<List<TimeSegment>> segmentsData = theControl.getSegmentSettings().stream()
                .filter(TimeSegmentSetting::isComplete)
                .map(segmentSetting -> submissionTimeSegmentReporter.reportForQr(
                        qr.getId(),
                        segmentSetting.getSegmentType(),
                        segmentSetting.getBasedType(),
                        segmentSetting.getPageId(),
                        segmentSetting.getBasedControlId(),
                        segmentSetting.getTargetControlId(),
                        theControl.getInterval(),
                        theControl.getMax(),
                        app
                )).collect(toImmutableList());

        return new QTimeSegmentPresentation(theControl.getInterval(), segmentsData);
    }
}
