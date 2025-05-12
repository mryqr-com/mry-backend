package com.mryqr.core.presentation.query.bar;

import com.mryqr.core.common.domain.report.CategorizedOptionSegment;
import com.mryqr.core.presentation.query.QControlPresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.mryqr.core.app.domain.page.control.ControlType.BAR;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QBarPresentation extends QControlPresentation {
    private List<List<CategorizedOptionSegment>> segmentsData;

    public QBarPresentation(List<List<CategorizedOptionSegment>> segmentsData) {
        super(BAR);
        this.segmentsData = segmentsData;
    }

}
