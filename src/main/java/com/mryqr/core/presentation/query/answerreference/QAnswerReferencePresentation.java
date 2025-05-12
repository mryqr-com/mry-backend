package com.mryqr.core.presentation.query.answerreference;

import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.core.presentation.query.QControlPresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.mryqr.core.app.domain.page.control.ControlType.ANSWER_REFERENCE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QAnswerReferencePresentation extends QControlPresentation {
    private DisplayValue value;

    public QAnswerReferencePresentation(DisplayValue value) {
        super(ANSWER_REFERENCE);
        this.value = value;
    }
}
