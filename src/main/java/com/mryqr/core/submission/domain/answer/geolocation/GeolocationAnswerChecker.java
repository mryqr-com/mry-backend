package com.mryqr.core.submission.domain.answer.geolocation;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.FGeolocationControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.answer.AbstractSubmissionAnswerChecker;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.control.ControlType.GEOLOCATION;
import static com.mryqr.core.common.exception.ErrorCode.OUT_OF_OFF_SET_RADIUS;

@Component
@RequiredArgsConstructor
public class GeolocationAnswerChecker extends AbstractSubmissionAnswerChecker {
    @Override
    public ControlType controlType() {
        return GEOLOCATION;
    }

    @Override
    protected Answer doCheckAnswer(Answer answer, Control control, QR qr, Page page, App app, String submissionId) {
        GeolocationAnswer theAnswer = (GeolocationAnswer) answer;
        FGeolocationControl theControl = (FGeolocationControl) control;

        if (theControl.isOffsetRestrictionEnabled() && qr.isPositioned()) {
            if (theAnswer.getGeolocation().distanceFrom(qr.getGeolocation()) > theControl.getOffsetRestrictionRadius()) {
                failAnswerValidation(OUT_OF_OFF_SET_RADIUS, control, "定位超出了允许范围:[" + control.getName() + "]。");
            }
        }

        return theControl.check(theAnswer);
    }
}
