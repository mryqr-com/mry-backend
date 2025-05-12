package com.mryqr.core.platetemplate.query;

import com.mryqr.core.app.domain.plate.PlateSetting;
import com.mryqr.core.common.domain.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QListPlateTemplate {
    private final String id;
    private final PlateSetting plateSetting;
    private final UploadedFile image;
    private final int order;
}
