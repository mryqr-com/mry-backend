package com.mryqr.core.presentation.query.submissionreference;

import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.core.presentation.query.QControlPresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.mryqr.core.app.domain.page.control.ControlType.SUBMISSION_REFERENCE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QSubmissionReferencePresentation extends QControlPresentation {
    private Map<String, DisplayValue> values;

    public QSubmissionReferencePresentation(Map<String, DisplayValue> values) {
        super(SUBMISSION_REFERENCE);
        this.values = values;
    }
}
