package com.mryqr.core.presentation.query.numberrangesegment;

import com.mryqr.common.domain.report.NumberRangeSegment;
import com.mryqr.core.presentation.query.QControlPresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.mryqr.core.app.domain.page.control.ControlType.NUMBER_RANGE_SEGMENT;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QNumberRangeSegmentPresentation extends QControlPresentation {
    private List<Double> numberRanges;
    private List<NumberRangeSegment> segments;

    public QNumberRangeSegmentPresentation(List<Double> numberRanges, List<NumberRangeSegment> segments) {
        super(NUMBER_RANGE_SEGMENT);
        this.numberRanges = numberRanges;
        this.segments = segments;
    }
}
