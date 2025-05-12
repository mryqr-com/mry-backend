package com.mryqr.core.presentation.query.doughnut;

import com.mryqr.core.common.domain.report.CategorizedOptionSegment;
import com.mryqr.core.presentation.query.QControlPresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.mryqr.core.app.domain.page.control.ControlType.DOUGHNUT;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QDoughnutPresentation extends QControlPresentation {
    private List<CategorizedOptionSegment> segments;

    public QDoughnutPresentation(List<CategorizedOptionSegment> segments) {
        super(DOUGHNUT);
        this.segments = segments;
    }
}
