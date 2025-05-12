package com.mryqr.core.presentation.query.attributetable;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PAttributeTableControl;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.presentation.query.AttributePresentationFetcher;
import com.mryqr.core.presentation.query.ControlPresentationer;
import com.mryqr.core.presentation.query.QControlPresentation;
import com.mryqr.core.qr.domain.QR;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.mryqr.core.app.domain.page.control.ControlType.ATTRIBUTE_TABLE;

@Component
@RequiredArgsConstructor
public class AttributeTablePresentationer implements ControlPresentationer {
    private final AttributePresentationFetcher attributePresentationFetcher;

    @Override
    public boolean canHandle(Control control) {
        return control.getType() == ATTRIBUTE_TABLE;
    }

    @Override
    public QControlPresentation present(QR qr, Control control, App app) {
        PAttributeTableControl theControl = (PAttributeTableControl) control;

        Map<String, DisplayValue> values = attributePresentationFetcher.fetchAttributePresentations(qr,
                theControl.getAttributeIds(),
                app);

        return new QAttributeTablePresentation(values);
    }
}
