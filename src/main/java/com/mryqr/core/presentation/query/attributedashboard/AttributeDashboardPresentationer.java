package com.mryqr.core.presentation.query.attributedashboard;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PAttributeDashboardControl;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.presentation.query.AttributePresentationFetcher;
import com.mryqr.core.presentation.query.ControlPresentationer;
import com.mryqr.core.presentation.query.QControlPresentation;
import com.mryqr.core.qr.domain.QR;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.mryqr.core.app.domain.page.control.ControlType.ATTRIBUTE_DASHBOARD;

@Component
@RequiredArgsConstructor
public class AttributeDashboardPresentationer implements ControlPresentationer {
    private final AttributePresentationFetcher attributePresentationFetcher;

    @Override
    public boolean canHandle(Control control) {
        return control.getType() == ATTRIBUTE_DASHBOARD;
    }

    @Override
    public QControlPresentation present(QR qr, Control control, App app) {
        PAttributeDashboardControl theControl = (PAttributeDashboardControl) control;

        Map<String, DisplayValue> values = attributePresentationFetcher.fetchAttributePresentations(qr,
                theControl.getAttributeIds(),
                app);

        return new QAttributeDashboardPresentation(values);
    }
}
