package com.mryqr.core.presentation.query.timesegment;

import com.mryqr.common.domain.report.TimeSegment;
import com.mryqr.common.domain.report.TimeSegmentInterval;
import com.mryqr.core.presentation.query.QControlPresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.mryqr.core.app.domain.page.control.ControlType.TIME_SEGMENT;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QTimeSegmentPresentation extends QControlPresentation {
    private TimeSegmentInterval interval;
    private List<List<TimeSegment>> segmentsData;

    public QTimeSegmentPresentation(TimeSegmentInterval interval, List<List<TimeSegment>> segmentsData) {
        super(TIME_SEGMENT);
        this.interval = interval;
        this.segmentsData = segmentsData;
    }

}
