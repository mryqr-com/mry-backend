package com.mryqr.core.presentation.query;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.qr.domain.QR;

public interface ControlPresentationer {
    boolean canHandle(Control control);

    QControlPresentation present(QR qr, Control control, App app);
}
