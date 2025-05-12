package com.mryqr.core.common.domain.display;


import com.mryqr.core.common.domain.UploadedFile;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.mryqr.core.common.domain.display.DisplayValueType.FILES_DISPLAY_VALUE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FilesDisplayValue extends DisplayValue {
    private List<UploadedFile> files;


    public FilesDisplayValue(String key, List<UploadedFile> files) {
        super(key, FILES_DISPLAY_VALUE);
        this.files = files;
    }
}
