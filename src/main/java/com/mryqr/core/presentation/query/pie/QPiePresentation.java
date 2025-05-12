package com.mryqr.core.presentation.query.pie;

import com.mryqr.core.common.domain.report.CategorizedOptionSegment;
import com.mryqr.core.presentation.query.QControlPresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.mryqr.core.app.domain.page.control.ControlType.PIE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QPiePresentation extends QControlPresentation {
    private List<CategorizedOptionSegment> segments;

    public QPiePresentation(List<CategorizedOptionSegment> segments) {
        super(PIE);
        this.segments = segments;
    }
}
