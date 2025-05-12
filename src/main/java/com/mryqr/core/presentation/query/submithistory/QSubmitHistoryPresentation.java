package com.mryqr.core.presentation.query.submithistory;

import com.mryqr.core.presentation.query.QControlPresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.mryqr.core.app.domain.page.control.ControlType.SUBMIT_HISTORY;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QSubmitHistoryPresentation extends QControlPresentation {
    private List<QSubmitHistorySubmission> submissions;

    public QSubmitHistoryPresentation(List<QSubmitHistorySubmission> submissions) {
        super(SUBMIT_HISTORY);
        this.submissions = submissions;
    }
}
