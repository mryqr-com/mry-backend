package com.mryqr.core.qr.domain.attribute;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.FilesDisplayValue;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@TypeAlias("FILES_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FilesAttributeValue extends AttributeValue {
    private List<UploadedFile> files;

    public FilesAttributeValue(Attribute attribute, List<UploadedFile> files) {
        super(attribute);
        this.files = files;
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return null;
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
        return new FilesDisplayValue(this.getAttributeId(), files);
    }

    @Override
    public boolean isFilled() {
        return isNotEmpty(files);
    }

    @Override
    public void clean(App app) {

    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        return null;
    }
}
