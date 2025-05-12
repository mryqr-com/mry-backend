package com.mryqr.core.presentation.query.trend;

import com.mryqr.core.presentation.query.QControlPresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.mryqr.core.app.domain.page.control.ControlType.TREND;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QTrendPresentation extends QControlPresentation {
    private List<QTrendDataSet> dataSets;

    public QTrendPresentation(List<QTrendDataSet> dataSets) {
        super(TREND);
        this.dataSets = dataSets;
    }
}
