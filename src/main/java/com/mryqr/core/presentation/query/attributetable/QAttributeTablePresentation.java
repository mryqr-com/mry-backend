package com.mryqr.core.presentation.query.attributetable;

import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.presentation.query.QControlPresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.mryqr.core.app.domain.page.control.ControlType.ATTRIBUTE_TABLE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QAttributeTablePresentation extends QControlPresentation {
    private Map<String, DisplayValue> values;

    public QAttributeTablePresentation(Map<String, DisplayValue> values) {
        super(ATTRIBUTE_TABLE);
        this.values = values;
    }
}
