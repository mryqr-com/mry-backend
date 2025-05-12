package com.mryqr.core.qr.domain.attribute;

import com.mryqr.common.domain.TextOption;
import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.TextOptionDisplayValue;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@TypeAlias("CIRCULATION_STATUS_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class CirculationStatusAttributeValue extends AttributeValue {
    private String optionId;

    public CirculationStatusAttributeValue(Attribute attribute, String optionId) {
        super(attribute);
        this.optionId = optionId;
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return Set.of(optionId);
    }

    @Override
    protected Double doGetIndexedSortableValue() {
        return null;
    }

    @Override
    protected Set<String> doGetSearchableValues() {
        return null;
    }

    @Override
    protected DisplayValue doGetDisplayValue(QrReferenceContext context) {
        return new TextOptionDisplayValue(this.getAttributeId(), optionId);
    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        return context.getApp().circulationOptions().stream()
                .filter(it -> Objects.equals(it.getId(), optionId))
                .map(TextOption::getName)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean isFilled() {
        return isNotBlank(optionId);
    }

    @Override
    public void clean(App app) {
        if (isBlank(this.optionId)) {
            this.optionId = null;
            return;
        }

        List<TextOption> options = app.circulationOptions();
        if (options.stream().noneMatch(it -> Objects.equals(it.getId(), optionId))) {
            this.optionId = null;
        }
    }
}
