package com.mryqr.core.presentation.query.attributedashboard;

import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.core.presentation.query.QControlPresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.mryqr.core.app.domain.page.control.ControlType.ATTRIBUTE_DASHBOARD;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QAttributeDashboardPresentation extends QControlPresentation {
    private Map<String, DisplayValue> values;

    public QAttributeDashboardPresentation(Map<String, DisplayValue> values) {
        super(ATTRIBUTE_DASHBOARD);
        this.values = values;
    }
}
